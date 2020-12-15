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
package edu.kit.datamanager.indexer.mapping;

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.util.IndexerUtil;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Utilities class for mapping files.
 */
public class MappingUtil {

  /**
   * Return codes.
   */
  public static final int SUCCESS = 0;
  public static final int FAILURE = Integer.MIN_VALUE;

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(MappingUtil.class);

  private ApplicationProperties configuration;

  @Autowired
  public MappingUtil(ApplicationProperties configuration) {
    this.configuration = configuration;
  }

  /**
   * Map the source file to a new file using a given mapping tool.
   *
   * @param mappingFile The absolute path to mapping file.
   * @param srcFile The absolute path to the source file.
   * @param resultFile The absolute path to the created mapping.
   *
   * @return Errorcode (0 = SUCCESS)
   * @exception if an error occurs.
   */
  public Optional<Path> mapFile(Path mappingFile, Path srcFile, String mapping) {
    Path resultFile;
    int returnCode = FAILURE;
    resultFile = IndexerUtil.createTempFile(mapping + "_", ".mapping");
    try {
      returnCode = mapFile(mappingFile, srcFile, resultFile, mapping);
    } catch (IndexerException ie) {
      throw ie;
    } finally {
      if (returnCode != SUCCESS) {
        IndexerUtil.removeFile(resultFile);
        resultFile = null;
      }
    }

    return Optional.ofNullable(resultFile);
  }

  /**
   * Map the source file to a new file using a given mapping tool.
   *
   * @param mappingFile The absolute path to mapping file.
   * @param srcFile The absolute path to the source file.
   * @param resultFile The absolute path to the created mapping.
   *
   * @return Errorcode (0 = SUCCESS)
   */
  public int mapFile(Path mappingFile, Path srcFile, Path resultFile, String mapping) {
    int returnValue;

    IMappingTool mappingTool = IMappingTool.getMappingTool(configuration, mapping);
    if (resultFile.toFile().exists() && ((resultFile.toFile().length() > 0) || !resultFile.toFile().canWrite())) {
      throw new IndexerException("Overwriting file '" + resultFile.toString() + "' is not allowed!");
    }
    returnValue = mappingTool.mapFile(mappingFile, srcFile, resultFile);

    return returnValue;
  }
}
