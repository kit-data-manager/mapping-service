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
import edu.kit.datamanager.exceptions.CustomInternalServerError;
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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
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
          @RequestPart(name = "record") final MappingRecord record,
          @RequestPart(name = "document") final MultipartFile document,
          HttpServletRequest request,
          HttpServletResponse response,
          UriComponentsBuilder uriBuilder) throws URISyntaxException {

    LOG.trace("Performing createRecord({},...).", record);

    if ((record.getId() == null) || (record.getMappingType() == null)){
      LOG.error("Mandatory attribute Id not found in record. Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory attributes mappingType and/or schemaId not found in record.");
    }

 
    LOG.trace("Setting random UUID as record id.");
    record.setId(UUID.randomUUID().toString());
    LOG.debug("Test for existing metadata record for given schema and resource");
    MappingRecord dummy = new MappingRecord();
    dummy.setId(record.getId());
    Example<MappingRecord> example = Example.of(dummy);
    Optional<MappingRecord> findOne = mappingRecordDao.findOne(example);
    if (findOne.isPresent()) {
      LOG.error("Conflict with existing metadata record!");
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Mapping record already exists! Please update existing record instead!");
    }


    String callerPrincipal = (String) AuthenticationHelper.getAuthentication().getPrincipal();
    LOG.trace("Checking resource for caller acl entry.");
    //check ACLs for caller
    AclEntry callerEntry = null;
    for (AclEntry entry : record.getAcl()) {
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

      record.getAcl().add(callerEntry);
    } else {
      LOG.debug("Ensuring ADMINISTRATE permissions for acl entry {}.", callerEntry);
      //make sure at least the caller has administrate permissions
      callerEntry.setPermission(PERMISSION.ADMINISTRATE);
    }

    LOG.trace("Persisting metadata record.");
    try {
      String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
      mappingService.createMapping(contentOfFile, record);
    } catch (IOException ioe) {
      throw new IndexerException("Error: Can't read content of document!");
    }

    LOG.trace("Get ETag of MappingRecord.");
    String etag = record.getEtag();

    LOG.trace("Schema record successfully persisted. Updating document URI.");
    fixMetadataDocumentUri(record);

    URI locationUri;
    locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(record.getId(), null, null)).toUri();

    LOG.trace("Schema record successfully persisted. Returning result.");
    return ResponseEntity.created(locationUri).eTag("\"" + etag + "\"").body(record);
  }

  @Override
  public ResponseEntity<MappingRecord> getMappingById(
          @PathVariable(value = "id") String id,
          WebRequest wr,
          HttpServletResponse hsr
  ) {
    LOG.trace("Performing getMappingById({}).", id);
    MappingRecord record = getMappingById(id);
    //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
    LOG.trace("Get ETag of MappingRecord.");
    String etag = record.getEtag();

    fixMetadataDocumentUri(record);
    LOG.trace("Document URI successfully updated. Returning result.");
    return ResponseEntity.ok().eTag("\"" + etag + "\"").body(record);
  }

  @Override
  public ResponseEntity getMappingDocumentById(
          @PathVariable(value = "id") String id,
          WebRequest wr,
          HttpServletResponse hsr
  ) {
    LOG.trace("Performing getMappingDocumentById({}).", id);

    LOG.trace("Obtaining mapping record with id {}.", id);
    MappingRecord record = getMappingById(id);

    URI mappingDocumentUri = URI.create(record.getMappingDocumentUri());

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
          Pageable pgbl,
          WebRequest wr,
          HttpServletResponse hsr,
          UriComponentsBuilder ucb
  ) {

    //if security is enabled, include principal in query
    LOG.debug("Performing query for records.");
    Page<MappingRecord> records = mappingRecordDao.findAll(pgbl);

    LOG.trace("Cleaning up schemaDocumentUri of query result.");
    List<MappingRecord> recordList = records.getContent();

    recordList.forEach((record) -> {
      fixMetadataDocumentUri(record);
    });

    String contentRange = ControllerUtils.getContentRangeHeader(pgbl.getPageNumber(), pgbl.getPageSize(), records.getTotalElements());

    return ResponseEntity.status(HttpStatus.OK).header("Content-Range", contentRange).body(records.getContent());
  }

  @Override
  public ResponseEntity updateMapping(
          @PathVariable("id") String id,
          @RequestPart(name = "record", required = false) MappingRecord record,
          @RequestPart(name = "document", required = false)
          final MultipartFile document,
          WebRequest request,
          HttpServletResponse response,
          UriComponentsBuilder uriBuilder
  ) {
    LOG.trace("Performing updateRecord({}, {}, {}).", id, record, "#document");

    if (record == null && document == null) {
      LOG.error("No metadata schema record provided. Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Neither metadata record nor metadata document provided.");
    }

    LOG.trace("Obtaining most recent metadata record with id {}.", id);
    MappingRecord existingRecord = getMappingById(id);
    //if authorization enabled, check principal -> return HTTP UNAUTHORIZED or FORBIDDEN if not matching

    LOG.trace("Checking provided ETag.");
    ControllerUtils.checkEtag(request, existingRecord);
    record = mergeRecords(existingRecord, record);
 
    if (document != null) {
      LOG.trace("Updating metadata document.");
     try {
      String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
      mappingService.updateMapping(contentOfFile, record);
    } catch (IOException ioe) {
      throw new IndexerException("Error: Can't read content of document!");
    }
    } else {
      mappingRecordDao.save(record);
    }
     

    LOG.trace("Metadata record successfully persisted. Updating document URI and returning result.");
    fixMetadataDocumentUri(record);
    String etag = record.getEtag();

    URI locationUri;
    locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(record.getId(), null, null)).toUri();

    return ResponseEntity.ok().location(locationUri).eTag("\"" + etag + "\"").body(record);
  }

  @Override
  public ResponseEntity deleteMapping(
          @PathVariable(value = "id") String id,
          WebRequest wr,
          HttpServletResponse hsr
  ) {
    LOG.trace("Performing deleteRecord({}).", id);

    try {
      LOG.trace("Obtaining most recent schema record with id {}.", id);
      MappingRecord existingRecord = getMappingById(id);
      LOG.trace("Checking provided ETag.");
      ControllerUtils.checkEtag(wr, existingRecord);

      LOG.trace("Removing schema from database.");
      mappingRecordDao.delete(existingRecord);
      LOG.trace("Deleting all metadata documents from disk.");

      URL mappingFolderUrl;
      try {
        mappingFolderUrl = new URL(indexerProperties.getMappingsLocation());
        Path p = Paths.get(Paths.get(mappingFolderUrl.toURI()).toAbsolutePath().toString(), existingRecord.getId());
        LOG.trace("Deleting schema file(s) from path.", p);
        FileUtils.deleteDirectory(p.toFile());

        LOG.trace("All metadata documents for record with id {} deleted.", id);
      } catch (URISyntaxException | IOException ex) {
        LOG.error("Failed to obtain schema document for schemaId {}. Please remove schema files manually. Skipping deletion.");
      }

    } catch (ResourceNotFoundException ex) {
      //exception is hidden for DELETE
      LOG.debug("No metadata schema with id {} found. Skipping deletion.", id);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  private MappingRecord getMappingById(String recordId) throws ResourceNotFoundException {
    //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
      LOG.trace("No record version provided. Reading schema record from database.");
      Optional<MappingRecord> record = mappingRecordDao.findById(recordId);
      if (!record.isPresent()) {
        LOG.error("No metadata record found for id {}. Returning HTTP 404.", recordId);
        throw new ResourceNotFoundException("No metadata record found for id " + recordId + ".");
      }
      return record.get();
    }


  public MappingRecord mergeRecords(MappingRecord managed, MappingRecord provided) {
    if (provided != null) {
      if (!Objects.isNull(provided.getId())) {
        LOG.trace("Updating pid from {} to {}.", managed.getId(), provided.getId());
        managed.setId(provided.getId());
      }

      if (!Objects.isNull(provided.getMappingType())) {
        LOG.trace("Updating related resource from {} to {}.", managed.getMappingType(), provided.getMappingType());
        managed.setMappingType(provided.getMappingType());
      }

//      if (!Objects.isNull(provided.getMappingDocumentUri())) {
//        LOG.trace("Updating schemaId from {} to {}.", managed.getMappingDocumentUri(), provided.getMappingDocumentUri());
//        managed.setMappingDocumentUri(provided.getMappingDocumentUri());
//      }

      //update acl
      if (provided.getAcl() != null) {
        LOG.trace("Updating record acl from {} to {}.", managed.getAcl(), provided.getAcl());
        managed.setAcl(provided.getAcl());
      }
    }
    return managed;
  }

  private void fixMetadataDocumentUri(MappingRecord record) {
    record.setMappingDocumentUri(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingDocumentById(record.getId(), null, null)).toUri().toString());
  }

  private String getUniqueRecordHash(MappingRecord record) {
    String hash = null;
    try {
      LOG.trace("Creating metadata record hash.");
      MessageDigest md = MessageDigest.getInstance("SHA1");
      md.update(record.getId().getBytes(), 0, record.getId().length());
      md.update(record.getMappingType().getBytes(), 0, record.getMappingType().length());
      hash = Hex.encodeHexString(md.digest());
    } catch (NoSuchAlgorithmException ex) {
      LOG.error("Failed to initialize SHA1 MessageDigest.", ex);
      throw new CustomInternalServerError("Failed to create metadata record hash.");
    }
    return hash;
  }
}
