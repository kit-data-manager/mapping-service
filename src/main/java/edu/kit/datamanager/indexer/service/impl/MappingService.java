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
import edu.kit.datamanager.indexer.exception.IndexerException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
  ApplicationProperties applicationProperties;
  /**
   * Path to directory holding all mapping files.
   */
  Path mappingsDirectory;

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
   * @param filename filename of the mapping
   *
   * @return Path to mapping file.
   */
  public Path saveMapping(String content, String filename) {
    boolean success = false;
    Path mappingFile = Paths.get(mappingsDirectory.toString(), filename);
    try {
      FileUtils.writeStringToFile(mappingFile.toFile(), content, StandardCharsets.UTF_8);

    } catch (IOException ex) {
      LOGGER.error("Error writing mapping file!", ex);
      throw new IndexerException("Error writing mapping file to '" + mappingFile.toAbsolutePath() + "'!", ex);
    }
    return mappingFile;
  }

  /**
   * Update content of mapping file and get the mapping location.
   *
   * @param content Content of the mapping file.
   * @param filename filename of the mapping
   *
   * @return Path to mapping file.
   */
  public Path updateMapping(String content, String filename) {
    Path mappingFile = Paths.get(mappingsDirectory.toString(), filename);
    try {
      if (mappingFile.toFile().exists()) {
        FileUtils.writeStringToFile(mappingFile.toFile(), content, StandardCharsets.UTF_8);
      } else {
        throw new IndexerException("Cannot update due to missing mapping file! ('" + filename + "')");
      }
    } catch (IOException ex) {
      LOGGER.error("Error writing mapping file!", ex);
      throw new IndexerException("Error writing mapping file to '" + mappingFile.toAbsolutePath() + "'!", ex);
    }
    return mappingFile;
  }

  private void init(ApplicationProperties applicationProperties) {
    if ((applicationProperties != null) && (applicationProperties.getMappingsLocation() != null)) {
      try {
        mappingsDirectory = Files.createDirectories(Paths.get(applicationProperties.getMappingsLocation())).toAbsolutePath();
      } catch (IOException e) {
        throw new IndexerException("Could not initialize directory '" + applicationProperties.getMappingsLocation() + "' for mapping.", e);
      }
    } else {
      throw new IndexerException("Could not initialize mapping directory due to missing location!");
    }
  }
}
