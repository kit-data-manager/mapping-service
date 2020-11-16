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
package edu.kit.datamanager.python.gemma;

import edu.kit.datamanager.clients.SimpleServiceClient;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.mapping.IMappingTool;
import edu.kit.datamanager.python.util.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

/**
 * Utilities class for GEMMA.
 */
public class GemmaMapping implements IMappingTool {

  /** Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(GemmaMapping.class);

  
  GemmaConfiguration gemmaConfiguration;

  public GemmaMapping(ApplicationProperties configuration) {
    gemmaConfiguration = new GemmaConfiguration();
    gemmaConfiguration.setGemmaLocation(configuration.getGemmaLocation());
    gemmaConfiguration.setPythonLocation(configuration.getPythonLocation());
  }

  /**
   * Map the source file to a new file using a given mapping tool.
   *
   * @param mappingFile The absolute path to mapping file.
   * @param srcFile The absolute path to the source file.
   * @param resultFile The absolute path to the created mapping.
    *
   * @return Errorcode (0 = SUCCESS)
   * @see edu.kit.datamanager.python.util.PythonUtils
   */
  public int mapFile(Path mappingFile, Path srcFile, Path resultFile){
    LOGGER.trace("Run gemma on '{}' with mapping '{}' -> '{}'", srcFile, mappingFile, resultFile);
     int returnCode = PythonUtils.run(gemmaConfiguration.getPythonLocation(), gemmaConfiguration.getGemmaLocation(), mappingFile.toAbsolutePath().toString(), srcFile.toAbsolutePath().toString(), resultFile.toAbsolutePath().toString());
   return returnCode;
  }

}
