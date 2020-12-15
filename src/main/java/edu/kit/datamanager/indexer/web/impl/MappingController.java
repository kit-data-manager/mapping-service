/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.indexer.web.impl;

import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.domain.acl.AclEntry;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.service.impl.MappingService;
import edu.kit.datamanager.indexer.web.IMappingController;
import edu.kit.datamanager.util.AuthenticationHelper;
import edu.kit.datamanager.util.ControllerUtils;
import io.swagger.v3.core.util.Json;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;
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

/**
 * Controller for managing mapping files.
 */
@Controller
@RequestMapping(value = "/api/v1/mapping")
public class MappingController implements IMappingController {

  private static final Logger LOG = LoggerFactory.getLogger(MappingController.class);

  @Autowired
  private ApplicationProperties indexerProperties;
  @Autowired
  private IMappingRecordDao mappingRecordDao;

  @Autowired
  private MappingService mappingService;

  @Override
  public ResponseEntity createMapping(
          @RequestPart(name = "record") final MultipartFile record,
          @RequestPart(name = "document") final MultipartFile document,
          HttpServletRequest request,
          HttpServletResponse response,
          UriComponentsBuilder uriBuilder) throws URISyntaxException {

    LOG.trace("Performing createRecord({},...).", record);

    MappingRecord recordDocument;
    try {
      if (record == null || record.isEmpty()) {
        throw new IOException();
      }
      recordDocument = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
    } catch (IOException ex) {
      LOG.error("No metadata record provided. Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No mapping record provided.");
    }
    if ((recordDocument.getMappingId() == null) || (recordDocument.getMappingType() == null)) {
      String message = "Mandatory attribute mappingId and/or mappingType not found in record. ";
      LOG.error(message + "Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    LOG.debug("Test for existing mapping record for given mappingId and mappingType.");
    Optional<MappingRecord> findOne = mappingRecordDao.findByMappingIdAndMappingType(recordDocument.getMappingId(), recordDocument.getMappingType());
    if (findOne.isPresent()) {
      LOG.error("Conflict with existing metadata record!");
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Mapping record already exists! Please update existing record instead!");
    }

    String callerPrincipal = (String) AuthenticationHelper.getAuthentication().getPrincipal();
    LOG.trace("Checking resource for caller acl entry.");
    //check ACLs for caller
    AclEntry callerEntry = null;
    for (AclEntry entry : recordDocument.getAcl()) {
      if (callerPrincipal.equals(entry.getSid())) {
        LOG.trace("Acl entry for caller {} found: {}", callerPrincipal, entry);
        callerEntry = entry;
        break;
      }
    }

    if (callerEntry == null) {
      LOG.debug("Adding caller entry with ADMINISTRATE permissions.");

      callerEntry = new AclEntry();
      callerEntry.setSid(callerPrincipal);
      callerEntry.setPermission(PERMISSION.ADMINISTRATE);

      recordDocument.getAcl().add(callerEntry);
    } else {
      LOG.debug("Ensuring ADMINISTRATE permissions for acl entry {}.", callerEntry);
      //make sure at least the caller has administrate permissions
      callerEntry.setPermission(PERMISSION.ADMINISTRATE);
    }

    LOG.trace("Persisting mapping and record.");
    try {
      String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
      mappingService.createMapping(contentOfFile, recordDocument);
    } catch (IOException ioe) {
      throw new IndexerException("Error: Can't read content of document!");
    }

    LOG.trace("Get mapping record.");
    recordDocument = mappingRecordDao.findByMappingIdAndMappingType(recordDocument.getMappingId(), recordDocument.getMappingType()).get();

    LOG.trace("Get ETag of MappingRecord.");
    String etag = recordDocument.getEtag();

    LOG.trace("Schema record successfully persisted. Updating document URI.");
    fixMappingDocumentUri(recordDocument);

    URI locationUri;
    locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(recordDocument.getMappingId(), recordDocument.getMappingType(), null, null, null)).toUri();

    LOG.trace("Schema record successfully persisted. Returning result.");
    return ResponseEntity.created(locationUri).eTag("\"" + etag + "\"").body(recordDocument);
  }

  @Override
  public ResponseEntity<MappingRecord> getMappingById(
          @PathVariable(value = "mappingId") String mappingId,
          @PathVariable(value = "mappingType") String mappingType,
          Pageable pgbl,
          WebRequest wr,
          HttpServletResponse hsr
  ) {
    LOG.trace("Performing getMappingById({}, {}).", mappingId, mappingType);
    MappingRecord record = getMappingById(mappingId, mappingType);
    //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
    LOG.trace("Get ETag of MappingRecord.");
    String etag = record.getEtag();

    fixMappingDocumentUri(record);
    LOG.trace("Document URI successfully updated. Returning result.");
    return ResponseEntity.ok().eTag("\"" + etag + "\"").body(record);
  }

  @Override
  public ResponseEntity getMappingDocumentById(
          @PathVariable(value = "mappingId") String mappingId,
          @PathVariable(value = "mappingType") String mappingType,
          WebRequest wr,
          HttpServletResponse hsr
  ) {
    LOG.trace("Performing getMappingDocumentById({}, {}).", mappingId, mappingType);

    LOG.trace("Obtaining mapping record with id {}/{}.", mappingId, mappingType);
    MappingRecord record = getMappingById(mappingId, mappingType);

    URI mappingDocumentUri = Paths.get(record.getMappingDocumentUri()).toUri();

    Path metadataDocumentPath = Paths.get(mappingDocumentUri);
    if (!Files.exists(metadataDocumentPath) || !Files.isRegularFile(metadataDocumentPath) || !Files.isReadable(metadataDocumentPath)) {
      LOG.trace("Metadata document at path {} either does not exist or is no file or is not readable. Returning HTTP NOT_FOUND.", metadataDocumentPath);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Metadata document on server either does not exist or is no file or is not readable.");
    }

    return ResponseEntity.
            ok().
            header(HttpHeaders.CONTENT_LENGTH, String.valueOf(metadataDocumentPath.toFile().length())).
            body(new FileSystemResource(metadataDocumentPath.toFile()));
  }

  @Override
  public ResponseEntity<List<MappingRecord>> getMappings(
          @RequestParam(value = "mappingId", required = false) String mappingId,
          @RequestParam(value = "mappingType", required = false) String mappingType,
          Pageable pgbl,
          WebRequest wr,
          HttpServletResponse hsr,
          UriComponentsBuilder ucb
  ) {

    //if security is enabled, include principal in query
    LOG.debug("Performing query for records.");
    Page<MappingRecord> records;
    if ((mappingId == null) && (mappingType == null)) {
      records = mappingRecordDao.findAll(pgbl);
    } else {
      records = mappingRecordDao.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId), Arrays.asList(mappingType), pgbl);
    }

    LOG.trace("Cleaning up schemaDocumentUri of query result.");
    List<MappingRecord> recordList = records.getContent();

    recordList.forEach((record) -> {
      fixMappingDocumentUri(record);
    });

    String contentRange = ControllerUtils.getContentRangeHeader(pgbl.getPageNumber(), pgbl.getPageSize(), records.getTotalElements());

    return ResponseEntity.status(HttpStatus.OK).header("Content-Range", contentRange).body(records.getContent());
  }

  @Override
  public ResponseEntity updateMapping(
          @PathVariable(value = "mappingId", required = true) String mappingId,
          @PathVariable(value = "mappingType", required = true) String mappingType,
          @RequestPart(name = "record", required = false) MultipartFile record,
          @RequestPart(name = "document", required = false)
          final MultipartFile document,
          WebRequest request,
          HttpServletResponse response,
          UriComponentsBuilder uriBuilder
  ) {
    LOG.trace("Performing updateMapping().", record);

    MappingRecord recordDocument;
    try {
      if (record == null || record.isEmpty()) {
        throw new IOException();
      }
      recordDocument = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
    } catch (IOException ex) {
      LOG.error("No metadata record provided. Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No mapping record provided.");
    }
    if ((recordDocument.getMappingId() == null) || (recordDocument.getMappingType() == null)) {
      String message = "Mandatory attribute mappingId and/or mappingType not found in record. ";
      LOG.error(message + "Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
    if ((!recordDocument.getMappingId().equals(mappingId)) || (!recordDocument.getMappingType().equals(mappingType))) {
      String message = "Mandatory attribute mappingId and/or mappingType are not identical to path parameters. "
              + " (" + recordDocument.getMappingId() + "<-->" + mappingId + ", " + recordDocument.getMappingType() + "<-->" + mappingType + ")";
      LOG.error(message + "Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);

    }
    LOG.trace("Obtaining most recent metadata record with id {}.", id);
    MappingRecord existingRecord = getMappingById(recordDocument.getMappingId(), recordDocument.getMappingType());
    //if authorization enabled, check principal -> return HTTP UNAUTHORIZED or FORBIDDEN if not matching

    LOG.trace("Checking provided ETag.");
    ControllerUtils.checkEtag(request, existingRecord);
    recordDocument = mergeRecords(existingRecord, recordDocument);

    if (document != null) {
      LOG.trace("Updating metadata document.");
      try {
        String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
        mappingService.updateMapping(contentOfFile, recordDocument);
      } catch (IOException ioe) {
        throw new IndexerException("Error: Can't read content of document!");
      }
    } else {
      mappingRecordDao.save(recordDocument);
    }

    LOG.trace("Metadata record successfully persisted. Updating document URI and returning result.");
    fixMappingDocumentUri(recordDocument);
    String etag = recordDocument.getEtag();

    URI locationUri;
    locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(recordDocument.getMappingId(), recordDocument.getMappingType(), null, null, null)).toUri();

    return ResponseEntity.ok().location(locationUri).eTag("\"" + etag + "\"").body(recordDocument);
  }

  @Override
  public ResponseEntity deleteMapping(
          @PathVariable(value = "mappingId") String mappingId,
          @PathVariable(value = "mappingType") String mappingType,
          WebRequest wr,
          HttpServletResponse hsr
  ) {
    LOG.trace("Performing deleteRecord({}).", id);

    try {
      LOG.trace("Obtaining most mapping record with {}/{}.", mappingId, mappingType);
      MappingRecord existingRecord = getMappingById(mappingId, mappingType);
      LOG.trace("Checking provided ETag.");
      ControllerUtils.checkEtag(wr, existingRecord);

      LOG.trace("Removing mapping from database and backup it on disc.");
      mappingService.deleteMapping(existingRecord);

    } catch (ResourceNotFoundException ex) {
      //exception is hidden for DELETE
      LOG.debug("No metadata schema with id {}/{} found. Skipping deletion.", mappingId, mappingType);
      throw ex;
    } catch (IOException ex) {
      LOG.error("Error removing mapping", ex);
      throw new IndexerException("Unknown error removing map!");
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
//
//  @Bean
//  public RestTemplate restTemplate() {
//    return new RestTemplate();
//  }

  /**
   * Get the record of given id / type.
   *
   * @param mappingId mappingId of the mapping
   * @param mappingType mappingType of the mapping
   * @return record of given id / type.
   * @throws ResourceNotFoundException Not found.
   */
  private MappingRecord getMappingById(String mappingId, String mappingType) throws ResourceNotFoundException {
    //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
    LOG.trace("Reading mapping record from database.");
    Optional<MappingRecord> record = mappingRecordDao.findByMappingIdAndMappingType(mappingId, mappingType);
    if (!record.isPresent()) {
      String message = String.format("No mapping record found for mapping %s/%s. Returning HTTP 404.", mappingId, mappingType);
      LOG.error(message);
      throw new ResourceNotFoundException(message);
    }
    return record.get();
  }

  public MappingRecord mergeRecords(MappingRecord managed, MappingRecord provided) {
    if (provided != null) {

      //update acl
      if (provided.getAcl() != null) {
        LOG.trace("Updating record acl from {} to {}.", managed.getAcl(), provided.getAcl());
        managed.setAcl(provided.getAcl());
      }
    }
    return managed;
  }

  private void fixMappingDocumentUri(MappingRecord record) {
    record.setMappingDocumentUri(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingDocumentById(record.getMappingId(), record.getMappingType(), null, null)).toUri().toString());
  }
}
