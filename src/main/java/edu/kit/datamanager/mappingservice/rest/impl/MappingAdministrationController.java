/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.rest.impl;

import com.google.common.base.Strings;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.AclEntry;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.exception.*;
import edu.kit.datamanager.mappingservice.impl.MappingService;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import edu.kit.datamanager.mappingservice.rest.IMappingAdministrationController;
import edu.kit.datamanager.mappingservice.rest.PluginInformation;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import edu.kit.datamanager.util.AuthenticationHelper;
import edu.kit.datamanager.util.ControllerUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.core.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing mapping files.
 *
 * @author maximilianiKIT
 */
@Controller
@RequestMapping(value = "/api/v1/mappingAdministration")
public class MappingAdministrationController implements IMappingAdministrationController {

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MappingAdministrationController.class);

    /**
     * Connection to the database of mapping records.
     */
    private final IMappingRecordDao mappingRecordDao;

    /**
     * The plugin manager.
     */
    private final PluginManager pluginManager;

    private final MeterRegistry meterRegistry;
    private final DistributionSummary documentsInSizeMetric;
    private final DistributionSummary documentsOutSizeMetric;
    /**
     * Connection to the executive logic of the mapping service.
     */
    private final MappingService mappingService;

    public MappingAdministrationController(IMappingRecordDao mappingRecordDao, PluginManager pluginManager, MappingService mappingService, MeterRegistry meterRegistry) {
        this.mappingRecordDao = mappingRecordDao;
        this.mappingService = mappingService;
        this.pluginManager = pluginManager;
        this.meterRegistry = meterRegistry;

        Gauge.builder("mapping_service.schemes_total", mappingRecordDao::count).register(meterRegistry);
        this.documentsInSizeMetric = DistributionSummary.builder("mapping_service.documents.input_size").baseUnit("bytes").register(meterRegistry);
        this.documentsOutSizeMetric = DistributionSummary.builder("mapping_service.documents.output_size").baseUnit("bytes").register(meterRegistry);
    }

    @Override
    public ResponseEntity<MappingRecord> createMapping(
            @RequestPart(name = "record") final MultipartFile record,
            @RequestPart(name = "document") final MultipartFile document,
            WebRequest wr,
            HttpServletResponse hsr,
            UriComponentsBuilder uriBuilder) {
        LOG.trace("Performing createMapping().");

        MappingRecord mappingRecord;
        try {
            mappingRecord = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
            LOG.trace("Deserialized mapping record: {}", record);
        } catch (IOException ex) {
            LOG.error("Unable to deserialize mapping record.", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (Strings.isNullOrEmpty(mappingRecord.getMappingId()) || Strings.isNullOrEmpty(mappingRecord.getMappingType())) {
            LOG.error("Invalid mapping record. Either mappingId or mappingType are null.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        LOG.trace("Obtaining caller principle for authorization purposes.");
        String callerPrincipal = (String) AuthenticationHelper.getAuthentication().getPrincipal();

        LOG.trace("Checking provided MappingRecord for acl entry of caller id {}.", callerPrincipal);
        //check ACLs for caller
        AclEntry callerEntry = null;
        for (AclEntry entry : mappingRecord.getAcl()) {
            if (callerPrincipal.equals(entry.getSid())) {
                LOG.trace("Acl entry for caller {} found: {}", callerPrincipal, entry);
                callerEntry = entry;
                break;
            }
        }

        if (callerEntry == null) {
            LOG.trace("No acl entry found. Adding caller entry with ADMINISTRATE permissions.");
            callerEntry = new AclEntry();
            callerEntry.setSid(callerPrincipal);
            callerEntry.setPermission(PERMISSION.ADMINISTRATE);
            mappingRecord.getAcl().add(callerEntry);
        } else {
            LOG.debug("Acl entry found. Ensuring ADMINISTRATE permissions for acl entry {}.", callerEntry);
            //make sure at least the caller has administrative permissions
            callerEntry.setPermission(PERMISSION.ADMINISTRATE);
        }

        try {
            LOG.trace("Reading mapping document.");
            String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
            LOG.trace("Mapping document successfully read.");
            mappingRecord = mappingService.createMapping(contentOfFile, mappingRecord);
        } catch (IOException ioe) {
            LOG.error("Unable to create mapping for provided inputs.", ioe);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        LOG.trace("Mapping successfully persisted. Updating document URI.");
        fixMappingDocumentUri(mappingRecord);

        URI locationUri;
        locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(mappingRecord.getMappingId(), wr, hsr)).toUri();

        LOG.trace("Successfully created mapping with id '{}' and type '{}'.", mappingRecord.getMappingId(), mappingRecord.getMappingType());
        return ResponseEntity.created(locationUri).body(mappingRecord);
    }

    @Override
    public ResponseEntity<MappingRecord> getMappingById(
            @PathVariable(value = "mappingId") String mappingId,
            WebRequest wr,
            HttpServletResponse hsr) {
        LOG.trace("Performing getMappingById({}).", mappingId);
        MappingRecord record = getMappingByIdInternal(mappingId);
        //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN

        /* 
        //TODO: Authorization ready for this case, but postponed for getMappings()...therefore, it remains deactivated here for the moment.
        LOG.trace("Obtaining caller principle for authorization purposes.");
        String callerPrincipal = (String) AuthenticationHelper.getAuthentication().getPrincipal();

        LOG.trace("Checking provided mapping record for acl entry of caller id {}.", callerPrincipal);
        //check ACLs for caller
        boolean canRead = false;
        for (AclEntry entry : record.getAcl()) {
            LOG.trace("Acl entry for principle {} found. Checking for READ permissions.", callerPrincipal);
            if (callerPrincipal.equals(entry.getSid()) && entry.getPermission().atLeast(PERMISSION.READ)) {
                canRead = true;
                break;
            }
        }
        if (!canRead) {
            LOG.error("Caller principle {} has no read permissions for mapping {}. Returning HTTP 403.", callerPrincipal, mappingId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Caller " + callerPrincipal + " has no read permissions for mapping with id " + mappingId + ".");
        } else {
            LOG.trace("Read permissions of caller {} for mapping id {} successfully approved.", callerPrincipal, mappingId);
        }*/
        LOG.trace("Get ETag of MappingRecord.");
        String etag = record.getEtag();

        fixMappingDocumentUri(record);
        LOG.trace("Document URI successfully updated. Returning result.");
        return ResponseEntity.ok().eTag("\"" + etag + "\"").body(record);
    }

    @Override
    public ResponseEntity<Resource> getMappingDocumentById(
            @PathVariable(value = "mappingId") String mappingId,
            WebRequest wr,
            HttpServletResponse hsr) {
        LOG.trace("Performing getMappingDocumentById({}).", mappingId);

        LOG.trace("Obtaining mapping record with id {}.", mappingId);
        MappingRecord record = getMappingByIdInternal(mappingId);

        LOG.trace("Obtaining mapping document URI.");
        URI mappingDocumentUri = Paths.get(record.getMappingDocumentUri()).toUri();

        LOG.trace("Obtaining local path for mapping document URI {}.", mappingDocumentUri);
        Path mappingDocumentPath = Paths.get(mappingDocumentUri);
        LOG.trace("Checking accessibility of local path {}.", mappingDocumentPath.toString());
        if (!Files.exists(mappingDocumentPath) || !Files.isRegularFile(mappingDocumentPath) || !Files.isReadable(mappingDocumentPath)) {
            LOG.trace("Mapping document at path {} either does not exist or is no file or is not readable. Returning HTTP INTERNAL_SERVER_ERROR.", mappingDocumentPath);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ByteArrayResource("Metadata document on server either does not exist or is no file or is not readable.".getBytes(StandardCharsets.UTF_8)));
        }

        LOG.trace("Get ETag of MappingRecord.");
        String etag = record.getEtag();

        LOG.trace("Mapping document found. Returning result.");
        return ResponseEntity.ok().eTag("\"" + etag + "\"").header(HttpHeaders.CONTENT_LENGTH, String.valueOf(mappingDocumentPath.toFile().length())).body(new FileSystemResource(mappingDocumentPath.toFile()));
    }

    @Override
    public ResponseEntity<List<MappingRecord>> getMappings(
            @RequestParam(value = "typeId", required = false) String typeId,
            Pageable pgbl,
            WebRequest wr,
            HttpServletResponse hsr,
            UriComponentsBuilder ucb) {
        //if security is enabled, include principal in query
        LOG.trace("Performing getMappings({}, {}).", typeId, pgbl);
        Page<MappingRecord> records;
        //try {
        if ((typeId == null)) {
            LOG.trace("No type provided. Querying for all mapping records.");
            records = mappingRecordDao.findAll(pgbl);
        } else {
            LOG.trace("Querying for mapping records for mapping type {}.", typeId);
            records = mappingRecordDao.findByMappingIdIn(List.of(typeId), pgbl);
        }

        List<MappingRecord> recordList = records.getContent();
        LOG.trace("Obtained {} mapping(s).", recordList.size());
        LOG.trace("Cleaning up schemaDocumentUri of query result.");
        recordList.forEach(this::fixMappingDocumentUri);

        String contentRange = ControllerUtils.getContentRangeHeader(pgbl.getPageNumber(), pgbl.getPageSize(), records.getTotalElements());
        if (!records.isEmpty()) {
            LOG.trace("Returning {} mapping record(s) in content range {}.", recordList.size(), contentRange);
            return ResponseEntity.status(HttpStatus.OK).header("Content-Range", contentRange).body(records.getContent());
        } else {
            LOG.trace("Returning empty list of results.");
            return ResponseEntity.status(HttpStatus.OK).header("Content-Range", contentRange).body(new ArrayList<>());
        }
    }

    @Override
    public ResponseEntity<Void> deleteMapping(
            @PathVariable(value = "mappingId") String mappingId,
            WebRequest wr,
            HttpServletResponse hsr) {
        LOG.trace("Performing deleteMapping({}).", mappingId);

        try {
            LOG.trace("Obtaining mapping record with {}.", mappingId);
            MappingRecord existingRecord = getMappingByIdInternal(mappingId);
            LOG.trace("Checking provided ETag.");
            ControllerUtils.checkEtag(wr, existingRecord);

            LOG.trace("Removing mapping with id {}.", mappingId);
            mappingService.deleteMapping(existingRecord);
            LOG.trace("Successfully deleted mapping with id '{}'.", mappingId);
        } catch (MappingNotFoundException ex) {
            //exception is hidden for DELETE
            LOG.debug("No mapping with id {} found. Skipping deletion.", mappingId);
        }

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<MappingRecord> updateMapping(
            @PathVariable(value = "mappingId") String mappingId,
            @RequestPart(name = "record") final MultipartFile record,
            @RequestPart(name = "document", required = false) final MultipartFile document,
            WebRequest wr,
            HttpServletResponse hsr,
            UriComponentsBuilder uriBuilder) {
        LOG.trace("Performing updateMapping {}", record);

        MappingRecord mappingRecord;
        try {
            mappingRecord = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
            LOG.trace("Deserialized mapping record: {}", record);
        } catch (IOException ex) {
            LOG.error("Unable to deserialize mapping record.", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if ((!mappingId.equals(mappingRecord.getMappingId()))) {
            LOG.error("Mapping record id {} differs from addressed mapping id {}.", mappingRecord.getMappingId(), mappingId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        LOG.trace("Reading mapping record with id {} from database.", mappingRecord.getMappingId());
        MappingRecord existingRecord = getMappingByIdInternal(mappingRecord.getMappingId());
        //if authorization enabled, check principal -> return HTTP UNAUTHORIZED or FORBIDDEN if not matching

        LOG.trace("Checking provided ETag.");
        ControllerUtils.checkEtag(wr, existingRecord);
        mappingRecord = mergeRecords(existingRecord, mappingRecord);

        if (document != null) {
            LOG.trace("User provided mapping document found.");
            try {
                LOG.trace("Reading mapping document.");
                String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
                LOG.trace("Mapping document successfully read.");
                mappingService.updateMapping(contentOfFile, mappingRecord);
            } catch (IOException ioe) {
                LOG.error("Unable to create mapping for provided inputs.", ioe);
                return ResponseEntity.internalServerError().build();
            }
        } else {
            LOG.trace("No mapping document provided by user. Only persisting updated mapping record.");
            mappingRecordDao.save(mappingRecord);
        }

        LOG.trace("Mapping update successfully persisted.");
        fixMappingDocumentUri(mappingRecord);
        String etag = mappingRecord.getEtag();

        URI locationUri;
        locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(mappingRecord.getMappingId(), wr, hsr)).toUri();

        LOG.info("Successfully updated mapping record with id '{}' and type '{}'.", mappingRecord.getMappingId(), mappingRecord.getMappingType());
        return ResponseEntity.ok().location(locationUri).eTag("\"" + etag + "\"").body(mappingRecord);
    }

    @Override
    public ResponseEntity<List<PluginInformation>> getAvailablePlugins(WebRequest wr, HttpServletResponse hsr) {
        LOG.trace("Performing getAvailablePlugins()");

        List<PluginInformation> plugins = new ArrayList<>();
        pluginManager.listPluginIds().forEach((id) -> {
            try {
                plugins.add(new PluginInformation(id, pluginManager));
            } catch (MappingPluginException ex) {
                LOG.error("Error getting plugin information for id " + id, ex);
            }
        });

        LOG.trace("Returning {} available plugin(s).", plugins.size());
        return ResponseEntity.ok().body(plugins);
    }

    @Override
    public ResponseEntity<String> reloadAvailablePlugins(WebRequest wr, HttpServletResponse hsr) {
        LOG.trace("Reloading available plugins.");
        pluginManager.reloadPlugins();
        LOG.trace("Plugins successfully reloaded.");
        return ResponseEntity.noContent().build();
    }

    @Override
    public void runPlugin(MultipartFile document, MultipartFile mapping, String typeID, HttpServletRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder) {
        LOG.trace("Performing mapDocument(File#{}, File#{}, {})", document.getOriginalFilename(), mapping.getOriginalFilename(), typeID);
        Optional<Path> resultPath = Optional.empty();

        if (document.isEmpty() || mapping.isEmpty() || typeID.isBlank()) {
            String message = "Either typeID, mapping document, or input document are missing. Unable to perform mapping.";
            LOG.error(message);
            throw new MappingServiceUserException(message);
        }

        LOG.trace("Obtaining plugin for id {}.", typeID);
        if (!pluginManager.listPluginIds().contains(typeID)) {
            String message = String.format("No plugin found for mapping id %s.", typeID);
            LOG.error("{}. Returning HTTP 404.", message);
            throw new PluginNotFoundException(message);
        }

        LOG.trace("Processing mapping input file.");
        String extension = "." + FilenameUtils.getExtension(document.getOriginalFilename());
        LOG.trace(" - Determined file extension: {}", extension);
        Path inputPath = FileUtil.createTempFile("inputMultipart", extension);
        LOG.trace(" - Writing user upload to: {}", inputPath);
        File inputFile = inputPath.toFile();
        try {
            document.transferTo(inputFile);
            LOG.trace("Successfully stored user upload at {}.", inputPath);
        } catch (IOException e) {
            LOG.error("Failed to store user upload.", e);
            throw new MappingExecutionException("Unable to write user upload to disk.");
        }

        LOG.trace("Processing mapping rules file.");
        String mappingExtension = "." + FilenameUtils.getExtension(mapping.getOriginalFilename());
        LOG.trace(" - Determined file extension: {}", mappingExtension);
        Path mappingInputPath = FileUtil.createTempFile("mappingInputMultipart", mappingExtension);
        LOG.trace(" - Writing user upload to: {}", mappingInputPath);
        File mappingFile = mappingInputPath.toFile();
        try {
            mapping.transferTo(mappingFile);
            LOG.trace("Successfully stored user upload at {}.", mappingFile);
        } catch (IOException e) {
            LOG.error("Failed to store user upload.", e);
            throw new MappingExecutionException("Unable to write user upload to disk.");
        }

        try {
            LOG.trace("Performing mapping process of file {} via mapping service", inputPath);

            resultPath = mappingService.runPlugin(inputFile.toURI(), mappingInputPath.toUri(), typeID);
            if (resultPath.isPresent()) {
                LOG.trace("Mapping process finished. Output written to {}.", resultPath.toString());
            } else {
                throw new MappingPluginException(MappingPluginState.UNKNOWN_ERROR(), "Mapping process finished, but no result was returned.");
            }
        } catch (MappingPluginException e) {
            LOG.error("Failed to execute mapping.", e);
            e.throwMe();
        } finally {
            LOG.trace("Removing user upload at {}.", inputFile);
            FileUtil.removeFile(inputPath);
            LOG.trace("User upload successfully removed.");
        }

        Path result = resultPath.get();
        if (!Files.exists(result) || !Files.isRegularFile(result) || !Files.isReadable(result)) {
            String message = "The mapping result expected at path " + result + " is not accessible. This indicates an error of the mapper implementation.";
            LOG.error(message);
            throw new MappingServiceException(message);
        }

        LOG.trace("Determining mime type for mapping result {}.", result);
        result = FileUtil.fixFileExtension(result);

        String mimeType = FileUtil.getMimeType(result);
        LOG.trace("Mime type {} determined. Identifying file extension.", mimeType);
        String resultExtension = FileUtil.getExtensionForMimeType(mimeType);

        LOG.trace("Returning result using mime type {} and file extension {}.", mimeType, extension);
        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", mimeType);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(result.toFile().length()));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;" + "filename=result" + extension);
        try {
            LOG.trace("Writing file to response output stream.");
            Files.copy(result, response.getOutputStream());
        } catch (IOException ex) {
            String message = "Failed to write mapping result file to stream.";
            LOG.error(message, ex);
            throw new MappingServiceException(message);
        } finally {
            Counter.builder("mapping_service.mapping_usage").tag("mappingID", typeID).register(meterRegistry).increment();
            this.documentsInSizeMetric.record(document.getSize());
            this.documentsOutSizeMetric.record(result.toFile().length());

            LOG.trace("Result file successfully transferred to client. Removing file {} from disk.", result);
            try {
                Files.delete(result);
                LOG.trace("Result file successfully removed.");
            } catch (IOException ignored) {
                LOG.warn("Failed to remove result file. Please remove manually.");
            }
        }
    }


    /**
     * Get the record of given id / type.
     *
     * @param mappingId mappingId of the mapping
     * @return record of given id / type.
     * @throws MappingNotFoundException Not found.
     */
    private MappingRecord getMappingByIdInternal(String mappingId) throws MappingNotFoundException {
        //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
        LOG.trace("Reading mapping record from database.");
        Optional<MappingRecord> record = mappingRecordDao.findByMappingId(mappingId);
        if (record.isEmpty()) {
            String message = String.format("No mapping record found for mapping %s. Returning HTTP 404.", mappingId);
            LOG.error(message);
            throw new MappingNotFoundException(message);
        }
        return record.get();
    }

    /**
     * This method merges two MappingRecords.
     *
     * @param managed  The existing MappingRecord.
     * @param provided The MappingRecord to merge.
     * @return The merged MappingRecord.
     */
    public MappingRecord mergeRecords(MappingRecord managed, MappingRecord provided) {
        if (provided != null) {
            LOG.trace("Updating record title from {} to {}.", managed.getTitle(), provided.getTitle());
            managed.setTitle(provided.getTitle());
            LOG.trace("Updating record description from {} to {}.", managed.getDescription(), provided.getDescription());
            managed.setDescription(provided.getDescription());
            LOG.trace("Updating record type from {} to {}.", managed.getMappingType(), provided.getMappingType());
            managed.setMappingType(provided.getMappingType());

            //update acl
            LOG.trace("Obtaining caller principle for authorization purposes.");
            String callerPrincipal = (String) AuthenticationHelper.getAuthentication().getPrincipal();

            LOG.trace("Checking provided MappingRecord for acl entry of caller id {}.", callerPrincipal);
            //check ACLs for caller
            AclEntry callerEntry = null;
            for (AclEntry entry : provided.getAcl()) {
                if (callerPrincipal.equals(entry.getSid())) {
                    LOG.trace("Acl entry for caller {} found: {}", callerPrincipal, entry);
                    callerEntry = entry;
                    break;
                }
            }

            if (callerEntry == null) {
                LOG.trace("No acl entry found. Adding caller entry with ADMINISTRATE permissions.");
                callerEntry = new AclEntry();
                callerEntry.setSid(callerPrincipal);
                callerEntry.setPermission(PERMISSION.ADMINISTRATE);
                provided.getAcl().add(callerEntry);
            } else {
                LOG.debug("Acl entry found. Ensuring ADMINISTRATE permissions for acl entry {}.", callerEntry);
                //make sure at least the caller has administrative permissions
                callerEntry.setPermission(PERMISSION.ADMINISTRATE);
            }

            LOG.trace("Updating record acl from {} to {}.", managed.getAcl(), provided.getAcl());
            managed.setAcl(provided.getAcl());
        }
        return managed;
    }

    private void fixMappingDocumentUri(MappingRecord record) {
        record.setMappingDocumentUri(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingDocumentById(record.getMappingId(), null, null)).toUri().toString());
    }
}
