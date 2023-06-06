/*
 * Copyright 2018 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.mappingservice.impl;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.exception.DuplicateMappingException;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.exception.MappingNotFoundException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

/**
 * Service for managing mappings.
 */
@Service
public class MappingService{

  /**
   * Repo holding all MappingRecords.
   */
  @Autowired
  private IMappingRecordDao mappingRepo;
  /**
   * Path to directory holding all mapping files.
   */
  private Path mappingsDirectory;

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(MappingService.class);

  @Autowired
  public MappingService(ApplicationProperties applicationProperties){
    init(applicationProperties);
  }

  /**
   * Save content to mapping file and get the mapping location.
   *
   * @param content Content of the mapping file.
   * @param mappingRecord record of the mapping
   *
   * @return The created mapping record.
   */
  public MappingRecord createMapping(String content, MappingRecord mappingRecord) throws IOException{
    LOGGER.trace("Creating mapping with id {}.", mappingRecord.getMappingId());
    Iterable<MappingRecord> findMapping = mappingRepo.findByMappingIdIn(Collections.singletonList(mappingRecord.getMappingId()));
    if(findMapping.iterator().hasNext()){
      LOGGER.error("Unable to create mapping with id {}. Mapping id is alreadyy used.", mappingRecord.getMappingId());
      mappingRecord = findMapping.iterator().next();
      throw new DuplicateMappingException("Error: Mapping '" + mappingRecord.getMappingType() + "_" + mappingRecord.getMappingId() + "' already exists!");
    }

    LOGGER.trace("Saving mapping file.");
    saveMappingFile(content, mappingRecord);
    LOGGER.trace("Persisting mapping record.");
    MappingRecord result = mappingRepo.save(mappingRecord);
    LOGGER.trace("Mapping with id {} successfully created.", result.getMappingId());
    return mappingRecord;
  }

  /**
   * Update content of mapping file and get the mapping location.
   *
   * @param content Content of the mapping file.
   * @param mappingRecord record of the mapping
   */
  public void updateMapping(String content, MappingRecord mappingRecord) throws IOException{
    Optional<MappingRecord> findMapping = mappingRepo.findByMappingId(mappingRecord.getMappingId());
    if(findMapping.isEmpty()){
      LOGGER.error("Failed to update mapping with id {}. Mapping not found.", mappingRecord.getMappingId());
      throw new MappingNotFoundException("Error: Mapping '" + mappingRecord.getMappingType() + "_" + mappingRecord.getMappingId() + "' doesn't exist!");
    }

    LOGGER.trace("Updating mapping with id {}.", mappingRecord.getMappingId());
    mappingRecord.setMappingDocumentUri(findMapping.get().getMappingDocumentUri());
    LOGGER.trace("Saving mapping file.");
    saveMappingFile(content, mappingRecord);
    LOGGER.trace("Persisting mapping record.");
    mappingRepo.save(mappingRecord);
    LOGGER.trace("Mapping with id {} successfully updated.", mappingRecord.getMappingId());

  }

  /**
   * Delete mapping file and its record.
   *
   * @param mappingRecord record of the mapping
   */
  public void deleteMapping(MappingRecord mappingRecord){
    Optional<MappingRecord> findMapping = mappingRepo.findByMappingId(mappingRecord.getMappingId());
    if(findMapping.isEmpty()){
      //deletion skipped, no error needed
      LOGGER.trace("Mapping with id {} not found. Skipping deletion.", mappingRecord.getMappingId());
      return;
    }
    LOGGER.trace("Deleting mapping with id {}.", mappingRecord.getMappingId());
    mappingRecord = findMapping.get();
    try{
      deleteMappingFile(mappingRecord);
    } catch(IOException e){
      LOGGER.error("Failed to delete mapping file at " + mappingRecord.getMappingDocumentUri() + ". Please remove it manually.", e);
    }
    mappingRepo.delete(mappingRecord);
    LOGGER.trace("Mapping with id {} deleted.", mappingRecord.getMappingId());
  }

  /**
   * Execute mapping and get the location of result file. If no according
   * mapping is found the src file will be returned.
   *
   * @param contentUrl Content of the src file.
   * @param mappingId id of the mapping
   * @return Path to result file.
   */
  public Optional<Path> executeMapping(URI contentUrl, String mappingId) throws MappingPluginException{
    LOGGER.trace("Executing mapping of content {} using mapping with id {}.", contentUrl, mappingId);
    Optional<Path> returnValue;
    Path srcFile = Paths.get(contentUrl);//FileUtil.downloadResource(contentUrl);
    MappingRecord mappingRecord;

    //if(download.isPresent()){
    // Path srcFile = download.get();
    // Get mapping file
    LOGGER.trace("Searching for mapping with id {}.", mappingId);
    Optional<MappingRecord> optionalMappingRecord = mappingRepo.findByMappingId(mappingId);
    if(optionalMappingRecord.isPresent()){
      LOGGER.trace("Mapping for id {} found. Creating temporary output file.");
      mappingRecord = optionalMappingRecord.get();
      Path mappingFile = Paths.get(mappingRecord.getMappingDocumentUri());
      // execute mapping
      Path resultFile;
      resultFile = FileUtil.createTempFile(mappingId + "_" + srcFile.hashCode(), ".result");
      LOGGER.trace("Temporary output file available at {}. Performing mapping.", resultFile);
      MappingPluginState result = PluginManager.soleInstance().mapFile(mappingRecord.getMappingType(), mappingFile, srcFile, resultFile);
      LOGGER.trace("Mapping returned with result {}. Returning result file.", result);
      returnValue = Optional.of(resultFile);
      // remove downloaded file
      FileUtil.removeFile(srcFile);
    } else{
      LOGGER.error("Unable to find mapping with id {}.", mappingId);
      throw new MappingNotFoundException("Unable to find mapping with id " + mappingId + ".");
    }
    /*} else{
      String message = contentUrl != null ? "Error: Downloading content from '" + contentUrl + "'!" : "Error: No URL provided!";
      throw new MappingException(message);
    }*/
    return returnValue;
  }

  /**
   * Initalize mappings directory and mappingUtil instance.
   *
   * @param applicationProperties Properties holding mapping directory setting.
   */
  private void init(ApplicationProperties applicationProperties){
    if((applicationProperties != null) && (applicationProperties.getMappingsLocation() != null)){
      try{
        mappingsDirectory = Files.createDirectories(new File(applicationProperties.getMappingsLocation().getPath()).getAbsoluteFile().toPath());
      } catch(IOException e){
        throw new MappingException("Could not initialize directory '" + applicationProperties.getMappingsLocation() + "' for mapping.", e);
      }
    } else{
      throw new MappingException("Could not initialize mapping directory due to missing location!");
    }
  }

  /**
   * Save mapping file to mapping directory.
   *
   * @param content Content of file.
   * @param mapping record of mapping
   * @throws IOException error writing file.
   */
  private void saveMappingFile(String content, MappingRecord mapping) throws IOException{
    Path newMappingFile;
    if((content != null) && (mapping != null) && (mapping.getMappingId() != null) && (mapping.getMappingType() != null)){
      LOGGER.debug("Storing mapping file with id '{}' and type '{}'", mapping.getMappingId(), mapping.getMappingType());
      LOGGER.trace("Content of mapping: '{}'", content);
      try{
        // 'delete' old file
        deleteMappingFile(mapping);
        newMappingFile = Paths.get(mappingsDirectory.toString(), mapping.getMappingId() + "_" + mapping.getMappingType() + ".mapping");
        LOGGER.trace("Write content to '{}'", newMappingFile);
        FileUtils.writeStringToFile(newMappingFile.toFile(), content, StandardCharsets.UTF_8);
        mapping.setMappingDocumentUri(newMappingFile.toString());
        byte[] data = content.getBytes();

        MessageDigest md = MessageDigest.getInstance("SHA256");
        md.update(data, 0, data.length);

        mapping.setDocumentHash("sha256:" + Hex.encodeHexString(md.digest()));
      } catch(NoSuchAlgorithmException ex){
        String message = "Failed to initialize SHA256 MessageDigest.";
        LOGGER.error(message, ex);
        throw new MappingException(message, ex);
      } catch(IllegalArgumentException iae){
        String message = "Error: Unkown mapping! (" + mapping.getMappingType() + ")";
        LOGGER.error(message, iae);
        throw new MappingException(message, iae);
      }
    } else{
      throw new MappingException("Error saving mapping file! (no content)");
    }
  }

  /**
   * Delete mapping file. The file will not be deleted. Add actual datetime as
   * suffix
   *
   * @param mapping record of mapping
   * @throws IOException error writing file.
   */
  private void deleteMappingFile(MappingRecord mapping) throws IOException{
    if((mapping != null) && (mapping.getMappingDocumentUri() != null)){
      LOGGER.debug("Delete mapping file '{}'", mapping.getMappingDocumentUri());
      Path deleteFile = Paths.get(mapping.getMappingDocumentUri());
      if(deleteFile.toFile().exists()){
        Path newFileName = Paths.get(deleteFile.getParent().toString(), deleteFile.getFileName() + date2String());
        LOGGER.trace("Move mapping file fo '{}'", newFileName);
        FileUtils.moveFile(deleteFile.toFile(), newFileName.toFile());
      }
    }
  }

  /**
   * Create string from actual date.
   *
   * @return formatted date.
   */
  private String date2String(){
    SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
    return sdf.format(new Date());
  }
}
