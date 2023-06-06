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
import edu.kit.datamanager.exceptions.ResourceNotFoundException;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.domain.AclEntry;
import edu.kit.datamanager.mappingservice.exception.MappingException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

/**
 * Controller for managing mapping files.
 *
 * @author maximilianiKIT
 */
@Controller
@RequestMapping(value = "/api/v1/mappingAdministration")
public class MappingAdministrationController implements IMappingAdministrationController{

  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(MappingAdministrationController.class);

  /**
   * Connection to the database of mapping records.
   */
  private final IMappingRecordDao mappingRecordDao;

  /**
   * Connection to the executive logic of the mapping service.
   */
  private final MappingService mappingService;

  public MappingAdministrationController(IMappingRecordDao mappingRecordDao, MappingService mappingService){
    this.mappingRecordDao = mappingRecordDao;
    this.mappingService = mappingService;
  }

  @Override
  public ResponseEntity createMapping(@RequestPart(name = "record") final MultipartFile record, @RequestPart(name = "document") final MultipartFile document, HttpServletRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder){
    LOG.trace("Performing createMapping().");

    MappingRecord recordDocument;
    try{
      recordDocument = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
      LOG.trace("Deserialized MappingRecord: {}", record);
    } catch(IOException ex){
      LOG.error("Unable to deserialize MappingRecord.", ex);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to deserialize provided mapping record.");
    }

    String callerPrincipal = (String) AuthenticationHelper.getAuthentication().getPrincipal();
    LOG.trace("Checking provided MappingRecord for acl entry of caller id {}.", callerPrincipal);

    //check ACLs for caller
    AclEntry callerEntry = null;
    for(AclEntry entry : recordDocument.getAcl()){
      if(callerPrincipal.equals(entry.getSid())){
        LOG.trace("Acl entry for caller {} found: {}", callerPrincipal, entry);
        callerEntry = entry;
        break;
      }
    }

    if(callerEntry == null){
      LOG.trace("No acl entry found. Adding caller entry with ADMINISTRATE permissions.");
      callerEntry = new AclEntry();
      callerEntry.setSid(callerPrincipal);
      callerEntry.setPermission(PERMISSION.ADMINISTRATE);
      recordDocument.getAcl().add(callerEntry);
    } else{
      LOG.debug("Acl entry found. Ensuring ADMINISTRATE permissions for acl entry {}.", callerEntry);
      //make sure at least the caller has administrative permissions
      callerEntry.setPermission(PERMISSION.ADMINISTRATE);
    }

    try{
      LOG.trace("Reading mapping document.");
      String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
      recordDocument = mappingService.createMapping(contentOfFile, recordDocument);
    } catch(IOException ioe){
      LOG.error("Unable to create mapping for provided inputs.", ioe);
      return ResponseEntity.internalServerError().body("Unable to create mapping for provided inputs.");
    }

    LOG.trace("Schema record successfully persisted. Updating document URI.");
    fixMappingDocumentUri(recordDocument);

    URI locationUri;
    locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(recordDocument.getMappingId())).toUri();

    LOG.trace("Successfully created mapping with id '{}' and type '{}'.", recordDocument.getMappingId(), recordDocument.getMappingType());
    return ResponseEntity.created(locationUri).body(recordDocument);
  }

  @Override
  public ResponseEntity<MappingRecord> getMappingById(@PathVariable(value = "mappingId") String mappingId, Pageable pgbl, WebRequest wr, HttpServletResponse hsr){
    LOG.trace("Performing getMappingById({}).", mappingId);
    MappingRecord record = getMappingById(mappingId);
    //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
    LOG.trace("Get ETag of MappingRecord.");
    String etag = record.getEtag();

    fixMappingDocumentUri(record);
    LOG.trace("Document URI successfully updated. Returning result.");
    return ResponseEntity.ok().eTag("\"" + etag + "\"").body(record);
  }

  @Override
  public ResponseEntity getMappingDocumentById(@PathVariable(value = "mappingId") String mappingId, WebRequest wr, HttpServletResponse hsr){
    LOG.trace("Performing getMappingDocumentById({}).", mappingId);

    LOG.trace("Obtaining mapping record with id {}.", mappingId);
    MappingRecord record = getMappingById(mappingId);

    URI mappingDocumentUri = Paths.get(record.getMappingDocumentUri()).toUri();

    Path mappingDocumentPath = Paths.get(mappingDocumentUri);
    if(!Files.exists(mappingDocumentPath) || !Files.isRegularFile(mappingDocumentPath) || !Files.isReadable(mappingDocumentPath)){
      LOG.trace("Mapping document at path {} either does not exist or is no file or is not readable. Returning HTTP INTERNAL_SERVER_ERROR.", mappingDocumentPath);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Metadata document on server either does not exist or is no file or is not readable.");
    }
    LOG.trace("Mapping document found. Returning result.");
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_LENGTH, String.valueOf(mappingDocumentPath.toFile().length())).body(new FileSystemResource(mappingDocumentPath.toFile()));
  }

  @Override
  public ResponseEntity<List<MappingRecord>> getMappings(@RequestParam(value = "typeId", required = false) String typeId, Pageable pgbl, WebRequest wr, HttpServletResponse hsr, UriComponentsBuilder ucb){
    //if security is enabled, include principal in query
    LOG.trace("Performing getMappings({}, {}).", typeId, pgbl);
    Page<MappingRecord> records;
    //try {
    if((typeId == null)){
      LOG.trace("No type provided. Querying for all mapping records.");
      records = mappingRecordDao.findAll(pgbl);
    } else{
      LOG.trace("Querying for mapping records for mapping type {}.", typeId);
      records = mappingRecordDao.findByMappingIdIn(List.of(typeId), pgbl);
    }
    LOG.trace("Cleaning up schemaDocumentUri of query result.");
    List<MappingRecord> recordList = records.getContent();

    recordList.forEach(this::fixMappingDocumentUri);

    String contentRange = ControllerUtils.getContentRangeHeader(pgbl.getPageNumber(), pgbl.getPageSize(), records.getTotalElements());
    if(!records.isEmpty()){
      LOG.trace("Returning {} mapping record(s).", recordList.size());
      return ResponseEntity.status(HttpStatus.OK).header("Content-Range", contentRange).body(records.getContent());
    }

    LOG.trace("No mapping records found for type {}. Returning empty list.", typeId);
    return ResponseEntity.status(HttpStatus.OK).body(new ArrayList<>());
  }

  @Override
  public ResponseEntity deleteMapping(@PathVariable(value = "mappingId") String mappingId, WebRequest wr, HttpServletResponse hsr){
    LOG.trace("Performing deleteMapping({}).", mappingId);

    try{
      LOG.trace("Obtaining mapping record with {}.", mappingId);
      MappingRecord existingRecord = getMappingById(mappingId);
      LOG.trace("Checking provided ETag.");
      ControllerUtils.checkEtag(wr, existingRecord);

      LOG.trace("Removing mapping from database and backup it on disc.");
      mappingService.deleteMapping(existingRecord);
      LOG.trace("Successfully deleted mapping record with id '{}'.", mappingId);
    } catch(ResourceNotFoundException ex){
      //exception is hidden for DELETE
      LOG.debug("No mapping with id {} found. Skipping deletion.", mappingId);
    }

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @Override
  public ResponseEntity updateMapping(@PathVariable(value = "mappingId") String mappingId, @RequestPart(name = "record") final MultipartFile record, @RequestPart(name = "document", required = false) final MultipartFile document, WebRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder){
    LOG.trace("Performing updateMapping {}", record);

    MappingRecord recordDocument;
    try{
      if(record == null || record.isEmpty()){
        throw new IOException();
      }
      recordDocument = Json.mapper().readValue(record.getInputStream(), MappingRecord.class);
    } catch(IOException ex){
      LOG.error("No metadata record provided. Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No mapping record provided.");
    }
    if((recordDocument.getMappingId() == null) || (recordDocument.getMappingType() == null)){
      String message = "Mandatory attribute mappingId and/or mappingType not found in record. ";
      LOG.error(message + "Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
    if((!recordDocument.getMappingId().equals(mappingId))){
      String message = "Mandatory attribute mappingId and/or mappingType are not identical to path parameters. " + " (" + recordDocument.getMappingId() + "<-->" + mappingId + ")";
      LOG.error(message + "Returning HTTP BAD_REQUEST.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);

    }
    LOG.trace("Obtaining most recent metadata record with id {}.", id);
    MappingRecord existingRecord = getMappingById(recordDocument.getMappingId());
    //if authorization enabled, check principal -> return HTTP UNAUTHORIZED or FORBIDDEN if not matching

    LOG.trace("Checking provided ETag.");
    ControllerUtils.checkEtag(request, existingRecord);
    recordDocument = mergeRecords(existingRecord, recordDocument);

    if(document != null){
      LOG.trace("Updating metadata document.");
      try{
        String contentOfFile = new String(document.getBytes(), StandardCharsets.UTF_8);
        mappingService.updateMapping(contentOfFile, recordDocument);
      } catch(IOException ioe){
        throw new MappingException("Error: Can't read content of document!");
      }
    } else{
      mappingRecordDao.save(recordDocument);
    }

    LOG.trace("Metadata record successfully persisted. Updating document URI and returning result.");
    fixMappingDocumentUri(recordDocument);
    String etag = recordDocument.getEtag();

    URI locationUri;
    locationUri = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingById(recordDocument.getMappingId())).toUri();

    LOG.info("Successfully updated mapping record with id '{}' and type '{}'.", recordDocument.getMappingId(), recordDocument.getMappingType());
    return ResponseEntity.ok().location(locationUri).eTag("\"" + etag + "\"").body(recordDocument);
  }

  @Override
  public ResponseEntity<List<PluginInformation>> getAllAvailableMappingTypes(WebRequest wr, HttpServletResponse hsr){
    List<PluginInformation> plugins = new ArrayList<>();
    PluginManager.soleInstance().getListOfAvailableValidators().forEach((id) -> {
      try{
        plugins.add(new PluginInformation(id));
      } catch(MappingPluginException ex){
        LOG.error("Error getting plugin information for {}", id, ex);
      }
    });
    return ResponseEntity.ok().body(plugins);
  }

  @Override
  public ResponseEntity<String> reloadAllAvailableMappingTypes(WebRequest wr, HttpServletResponse hsr){
    PluginManager.soleInstance().reloadPlugins();
    return ResponseEntity.noContent().build();
  }

  /**
   * Get the record of given id / type.
   *
   * @param mappingId mappingId of the mapping
   * @return record of given id / type.
   * @throws ResourceNotFoundException Not found.
   */
  public MappingRecord getMappingById(String mappingId) throws ResourceNotFoundException{
    //if security enabled, check permission -> if not matching, return HTTP UNAUTHORIZED or FORBIDDEN
    LOG.trace("Reading mapping record from database.");
    Optional<MappingRecord> record = mappingRecordDao.findByMappingId(mappingId);
    if(record.isEmpty()){
      String message = String.format("No mapping record found for mapping %s. Returning HTTP 404.", mappingId);
      LOG.error(message);
      throw new ResourceNotFoundException(message);
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
  public MappingRecord mergeRecords(MappingRecord managed, MappingRecord provided){
    if(provided != null){
      if(provided.getTitle() != null){
        LOG.trace("Updating record acl from {} to {}.", managed.getTitle(), provided.getTitle());
        managed.setTitle(provided.getTitle());
      }
      if(provided.getDescription() != null){
        LOG.trace("Updating record acl from {} to {}.", managed.getDescription(), provided.getDescription());
        managed.setDescription(provided.getDescription());
      }
      if(provided.getMappingType() != null){
        LOG.trace("Updating record acl from {} to {}.", managed.getMappingType(), provided.getMappingType());
        managed.setMappingType(provided.getMappingType());
      }
      //update acl
      if(provided.getAcl() != null){
        LOG.trace("Updating record acl from {} to {}.", managed.getAcl(), provided.getAcl());
        managed.setAcl(provided.getAcl());
      }
    }
    return managed;
  }

  private void fixMappingDocumentUri(MappingRecord record){
    record.setMappingDocumentUri(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(this.getClass()).getMappingDocumentById(record.getMappingId(), null, null)).toUri().toString());
  }
}
