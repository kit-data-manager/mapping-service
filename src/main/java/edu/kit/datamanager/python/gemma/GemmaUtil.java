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

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.python.util.*;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
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
   * Run the script at 'scriptLocation' with 'arguments' using the Python
   * executable at 'pythonLocation'. All output will be redirected to stdout and
   * stderr.
   *
   * @param mappingFile The absolute path to mapping file.
   * @param srcFile The absolute path to the source file.
   * @param resultFile The absolute path to the created mapping.
   * @param arguments Veriable number of arguments, which can also be omitted.
   *
   * @return The exit status of the python process or one of the internal codes
   * PYTHON_NOT_FOUND, TIMEOUT_ERROR or EXECUTION_ERROR.
   * @see edu.kit.datamanager.python.util.PythonUtils
   */
  public int runGemma(Path mappingFile, Path srcFile, Path resultFile){
    System.out.println("run GEMMA");
    System.out.println("+++++++++++++++++++++++++++++++++++++++++");
    System.out.println("Python: " + gemmaConfiguration.getPythonLocation());
    System.out.println("+++++++++++++++++++++++++++++++++++++++++");
    LOGGER.trace("Run gemma on '{}' with mapping '{}'", srcFile, mappingFile);
     int returnCode = PythonUtils.run(gemmaConfiguration.getPythonLocation(), gemmaConfiguration.getGemmaLocation(), mappingFile.toAbsolutePath().toString(), srcFile.toAbsolutePath().toString(), resultFile.toAbsolutePath().toString());
   return returnCode;
  }

}
