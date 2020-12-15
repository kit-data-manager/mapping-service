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
import edu.kit.datamanager.python.gemma.GemmaMapping;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for mapping tools. All available mapping tools have to be
 * 'registered' here.
 */
public interface IMappingTool {

  /**
   * Map holding all mapping tools.
   */
  public static Map<Mapping, IMappingTool> toolMapper = new HashMap<>();

  /**
   * Get mapping tool for give mapping.
   *
   * @param applicationProperties instance holding all properties for all
   * mapping tools.
   * @param mapping mapping which should be used.
   * @return instance for mapping file
   * @throws IndexerException if not a valid mapping.
   */
  public static IMappingTool getMappingTool(ApplicationProperties applicationProperties, String mapping) {
    Mapping map = null;
    try {
      map = Mapping.valueOf(mapping);
      if (!toolMapper.containsKey(map)) {
        switch (map) {
          case GEMMA:
            toolMapper.put(map, new GemmaMapping(applicationProperties));
            break;
          default:
            throw new IndexerException("Error: Mapping '" + mapping + "' is not registered yet!");
        }
      }
    } catch (Exception ex) {
      throw new IndexerException("Error: '" + mapping + "' is not a valid mapping!", ex);
    }
    return toolMapper.get(map);
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
  public int mapFile(Path mappingFile, Path srcFile, Path resultFile);

}
