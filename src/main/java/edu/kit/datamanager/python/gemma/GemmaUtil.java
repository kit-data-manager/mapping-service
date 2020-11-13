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
public class GemmaUtil{

  /** Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(GemmaUtil.class);

  
  GemmaConfiguration gemmaConfiguration;

  public GemmaUtil(GemmaConfiguration gemmaConfiguration) {
    this.gemmaConfiguration = gemmaConfiguration;
  }


  /**
   * Downloads the file behind the given URI and returns its path on local disc.
   * You should delete or move to another location afterwards.
   * 
   * @param resourceURL the given URI
   * @return the path to the created file. 
   */
  public Optional<Path> downloadResource(URI resourceURL) {
    String content = null;
    Path downloadedFile = null;
    try {
    content = SimpleServiceClient
    .create(resourceURL.toString())
    .accept(MediaType.TEXT_PLAIN)
    .getResource(String.class);
      downloadedFile = Files.createTempFile("gemma", "txt");
      FileUtils.writeStringToFile(downloadedFile.toFile(), content, StandardCharsets.UTF_8);
    } catch (Throwable tw) {
      LOGGER.error("Error reading URI '" + resourceURL.toString() + "'", tw);
    }
    return Optional.ofNullable(downloadedFile);
  }
  /**
   * Run the script at 'scriptLocation' with 'arguments' using the Python
   * executable at 'pythonLocation'. All output will be redirected to stdout and
   * stderr.
   *
   * @param mappingFile The absolute path to mapping file.
   * @param srcFile The absolute path to the source file.
   * @param resultFile The absolute path to the created mapping.
    *
   * @return The exit status of the python process or one of the internal codes
   * PYTHON_NOT_FOUND, TIMEOUT_ERROR or EXECUTION_ERROR.
   * @see edu.kit.datamanager.python.util.PythonUtils
   */
  public int runGemma(Path mappingFile, Path srcFile, Path resultFile){
    LOGGER.trace("Run gemma on '{}' with mapping '{}' -> '{}'", srcFile, mappingFile, resultFile);
     int returnCode = PythonUtils.run(gemmaConfiguration.getPythonLocation(), gemmaConfiguration.getGemmaLocation(), mappingFile.toAbsolutePath().toString(), srcFile.toAbsolutePath().toString(), resultFile.toAbsolutePath().toString());
   return returnCode;
  }

}
