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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Utilities class for python.
 *
 * @author jejkal
 */
public class MappingUtil {

  /**
   * Return codes.
   */
  public static final int SUCCESS = 0;

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(MappingUtil.class);

  private ApplicationProperties configuration;
  
  @Autowired
  public MappingUtil(ApplicationProperties configutation) {
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
   */
  public int mapFile(Path mappingFile, Path srcFile, Path resultFile, String mapping) {
    int returnValue;
    
    IMappingTool mappingTool = IMappingTool.getMappingTool(configuration, mapping);
    returnValue = mappingTool.mapFile(mappingFile, srcFile, resultFile);
    
    return returnValue;
  }
}
