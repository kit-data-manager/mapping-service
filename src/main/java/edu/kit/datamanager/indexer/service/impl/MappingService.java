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
package edu.kit.datamanager.indexer.service.impl;

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.mapping.Mapping;
import edu.kit.datamanager.indexer.mapping.MappingUtil;
import edu.kit.datamanager.indexer.util.IndexerUtil;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing mappings.
 */
@Service
public class MappingService {

  /**
   * Instance holding all settings.
   */
  private final ApplicationProperties applicationProperties;

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
   * MappingUtil for executing mappings.
   */
  private MappingUtil mappingUtil;

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(MappingService.class);

  @Autowired
  public MappingService(ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
    init(applicationProperties);
  }

  /**
   * Save content to mapping file and get the mapping location.
   *
   * @param content Content of the mapping file.
   * @param mappingRecord record of the mapping
   */
  public void createMapping(String content, MappingRecord mappingRecord) throws IOException {
    Optional<MappingRecord> findMapping = mappingRepo.findById(mappingRecord.getId());
    if (findMapping.isPresent()) {
      throw new IndexerException("Error: Mapping '" + mappingRecord.getId() + "' already exists!");
    }
    saveMappingFile(content, mappingRecord);
    mappingRepo.save(mappingRecord);
  }

  /**
   * Update content of mapping file and get the mapping location.
   *
   * @param content Content of the mapping file.
   * @param mappingRecord record of the mapping
   */
  public void updateMapping(String content, MappingRecord mappingRecord) throws IOException {
    Optional<MappingRecord> findMapping = mappingRepo.findById(mappingRecord.getId());
    if (!findMapping.isPresent()) {
      throw new IndexerException("Error: Mapping '" + mappingRecord.getId() + "' doesn't exists!");
    }
    mappingRecord.setMappingDocumentUri(findMapping.get().getMappingDocumentUri());
    saveMappingFile(content, mappingRecord);
    mappingRepo.save(mappingRecord);
  }

  /**
   * Delete mapping file and its record.
   *
  * @param mappingRecord record of the mapping
   */
  public void deleteMapping(MappingRecord mappingRecord) throws IOException {
    Optional<MappingRecord> findMapping = mappingRepo.findById(mappingRecord.getId());
    if (!findMapping.isPresent()) {
      throw new IndexerException("Error: Mapping '" + mappingRecord.getId() + "' doesn't exists!");
    }
    mappingRecord = findMapping.get();
    deleteMappingFile(mappingRecord);
    mappingRepo.delete(mappingRecord);
  }

  /**
   * Save content to mapping file and get the mapping location.
   *
   * @param content Content of the mapping file.
   * @param mappingId filename of the mapping
   *
   * @return Path to mapping file.
   */
  public Optional<Path> executeMapping(URI contentUrl, String mappingId) {
    Optional<Path> returnValue = Optional.ofNullable(null);

    Optional<MappingRecord> findMapping = mappingRepo.findById(mappingId);
    if (findMapping.isPresent()) {
      // create temporary file for content
      Optional<Path> download = IndexerUtil.downloadResource(contentUrl);
      if (download.isPresent()) {
        Path srcFile = download.get();
        // Get mapping file
        MappingRecord mappingRecord = findMapping.get();
        mappingRecord.getMappingDocumentUri();
        Path mappingFile = Paths.get(mappingRecord.getMappingDocumentUri());
        // execute mapping
        returnValue = mappingUtil.mapFile(mappingFile, srcFile, mappingId);
        // remove downloaded file
        IndexerUtil.removeFile(srcFile);
      } else {
        throw new IndexerException("Error: Unknown mapping '" + mappingId + "'!");
      }
    }
    return returnValue;
  }

  /**
   * Initalize mappings directory and mappingUtil instance.
   *
   * @param applicationProperties Properties holding mapping directory setting.
   */
  private void init(ApplicationProperties applicationProperties) {
    if ((applicationProperties != null) && (applicationProperties.getMappingsLocation() != null)) {
      mappingUtil = new MappingUtil(applicationProperties);
      try {
        mappingsDirectory = Files.createDirectories(Paths.get(applicationProperties.getMappingsLocation())).toAbsolutePath();
      } catch (IOException e) {
        throw new IndexerException("Could not initialize directory '" + applicationProperties.getMappingsLocation() + "' for mapping.", e);
      }
    } else {
      throw new IndexerException("Could not initialize mapping directory due to missing location!");
    }
  }

  /**
   * Save mapping file to mapping directory.
   *
   * @param content Content of file.
   * @param mapping record of mapping
   * @throws IOException error writing file.
   */
  private void saveMappingFile(String content, MappingRecord mapping) throws IOException {
    Path newMappingFile = null;
    if ((content != null) && (mapping != null) && (mapping.getId() != null) && (mapping.getMappingType()!= null)) {
      try {
        Mapping mappingType = Mapping.valueOf(mapping.getMappingType());
        // 'delete' old file
        deleteMappingFile(mapping);
        newMappingFile = Paths.get(mappingsDirectory.toString(), mapping.getId() + "_" + mapping.getMappingType() + ".mapping");
        FileUtils.writeStringToFile(newMappingFile.toFile(), content, StandardCharsets.UTF_8);
        mapping.setMappingDocumentUri(newMappingFile.toString());
        byte[] data = content.getBytes();
        
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(data, 0, data.length);
        
        mapping.setDocumentHash("sha1:" + Hex.encodeHexString(md.digest()));
      } catch (NoSuchAlgorithmException ex) {
          LOGGER.error("Failed to initialize SHA1 MessageDigest.", ex);
        throw new IndexerException("Failed to initialize SHA1 MessageDigest.", ex);
      } catch (IllegalArgumentException iae) {
        throw new IndexerException("Error: Unknown mapping type '" + mapping.getMappingType() + "'!");
      }
    } else {
      throw new IndexerException("Error saving mapping file! (no content)");
    }
  }

  /**
   * Delete mapping file. The file will not be deleted. Add actual datetime as
   * suffix
   *
   * @param mapping record of mapping
   * @throws IOException error writing file.
   */
  private void deleteMappingFile(MappingRecord mapping) throws IOException {
    if ((mapping != null) && (mapping.getMappingDocumentUri() != null)) {
      Path deleteFile = Paths.get(mapping.getMappingDocumentUri());
      if (deleteFile.toFile().exists()) {
        Path newFileName = Paths.get(deleteFile.getParent().toString(), deleteFile.getFileName() + date2String());
        FileUtils.moveFile(deleteFile.toFile(), newFileName.toFile());
      }
    }
  }

  /**
   * Create string from actual date.
   *
   * @return formatted date.
   */
  private String date2String() {
    SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
    String dateAsString = sdf.format(new Date());

    return dateAsString;
  }
}
