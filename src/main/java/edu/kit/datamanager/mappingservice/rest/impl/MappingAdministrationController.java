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

import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.domain.AclEntry;
import edu.kit.datamanager.mappingservice.exception.MappingNotFoundException;
import edu.kit.datamanager.mappingservice.impl.MappingService;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import edu.kit.datamanager.mappingservice.rest.IMappingAdministrationController;
import edu.kit.datamanager.mappingservice.rest.PluginInformation;
import edu.kit.datamanager.util.AuthenticationHelper;
import edu.kit.datamanager.util.ControllerUtils;
import io.swagger.v3.core.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import jakarta.servlet.http.HttpServletResponse;
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

    /**
     * Connection to the executive logic of the mapping service.
     */
    private final MappingService mappingService;

    public MappingAdministrationController(IMappingRecordDao mappingRecordDao, PluginManager pluginManager, MappingService mappingService) {
        this.mappingRecordDao = mappingRecordDao;
        this.mappingService = mappingService;
        this.pluginManager = pluginManager;
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
            LOG.trace("Deserializing mapping record.");
            mappingRecord = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
            LOG.trace("Deserialized mapping record: {}", record);
        } catch (IOException ex) {
            LOG.error("Unable to deserialize mapping record.", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//.body("Unable to deserialize provided mapping record.");
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
           //return ResponseEntity.internalServerError().body("Unable to create mapping for provided inputs.");
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        LOG.trace("Mapping successfully persisted. Updating document URI.");
        fixMappingDocumentUri(mappingRecord);

        URI locationUri;
        locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(mappingRecord.getMappingId(), wr, hsr)).toUri();

        LOG.trace("Successfully created mapping with id '{}' and type '{}'.", mappingRecord.getMappingId(), mappingRecord.getMappingType());
        return ResponseEntity.created(locationUri).body(mappingRecord);
    }

    @Override
    public ResponseEntity getMappingById(
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
    public ResponseEntity getMappingDocumentById(
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Metadata document on server either does not exist or is no file or is not readable.");
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
    public ResponseEntity deleteMapping(
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

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity updateMapping(
            @PathVariable(value = "mappingId") String mappingId,
            @RequestPart(name = "record") final MultipartFile record,
            @RequestPart(name = "document", required = false) final MultipartFile document,
            WebRequest wr,
            HttpServletResponse hsr,
            UriComponentsBuilder uriBuilder) {
        LOG.trace("Performing updateMapping {}", record);

        MappingRecord mappingRecord;
        try {
            LOG.trace("Deserializing mapping record.");
            mappingRecord = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
            LOG.trace("Deserialized mapping record: {}", record);
        } catch (IOException ex) {
            LOG.error("Unable to deserialize mapping record.", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to deserialize provided mapping record.");
        }

        /*Should never be null due to entity constraints
        if ((mappingRecord.getMappingId() == null) || (mappingRecord.getMappingType() == null)) {
            String message = "Mandatory attribute mappingId and/or mappingType not found in record. ";
            LOG.error(message + "Returning HTTP BAD_REQUEST.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }*/
        if ((!mappingRecord.getMappingId().equals(mappingId))) {
            LOG.trace("Mapping record id {} differs from adressed mapping id {}. Setting mapping record id to adressed id.", mappingRecord.getMappingId(), mappingId);
            mappingRecord.setMappingId(mappingId);
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
                return ResponseEntity.internalServerError().body("Unable to create mapping for provided inputs.");
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
    public ResponseEntity<List<PluginInformation>> getAllAvailableMappingTypes(WebRequest wr, HttpServletResponse hsr) {
        LOG.trace("Performing getAllAvailableMappingTypes()");

        List<PluginInformation> plugins = new ArrayList<>();
        pluginManager.getListOfAvailableValidators().forEach((id) -> {
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
    public ResponseEntity<String> reloadAllAvailableMappingTypes(WebRequest wr, HttpServletResponse hsr) {
        LOG.trace("Reloading available plugins.");
        pluginManager.reloadPlugins();
        LOG.trace("Plugins successfully reloaded.");
        return ResponseEntity.noContent().build();
    }

    /**
     * Get the record of given id / type.
     *
     * @param mappingId mappingId of the mapping
     *
     * @return record of given id / type.
     *
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
     * @param managed The existing MappingRecord.
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
