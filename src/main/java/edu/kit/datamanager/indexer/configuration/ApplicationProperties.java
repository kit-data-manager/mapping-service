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
package edu.kit.datamanager.indexer.configuration;

import edu.kit.datamanager.configuration.GenericPluginProperties;
import java.net.URL;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 */
@ConfigurationProperties(prefix = "indexer")
@Component
@Data
@Validated
@EqualsAndHashCode(callSuper = true)
public class ApplicationProperties extends GenericPluginProperties {

  /**
   * The absolute path to the python interpreter.
   */
  @edu.kit.datamanager.annotations.ExecutableFileURL
  @Value("${metastore.indexer.gemma.pythonLocation}")
  private URL pythonLocation;

  /**
   * The path to the gemma mapping script 'mapping_single.py'
   */
  @edu.kit.datamanager.annotations.LocalFileURL
  @Value("${metastore.indexer.gemma.gemmaLocation}")
  private URL gemmaLocation;

  /**
   * The absolute path where the mappings are stored.
   */
  @edu.kit.datamanager.annotations.LocalFolderURL
  @Value("${metastore.indexer.mappingsLocation}")
  private URL mappingsLocation;

  /**
   * The base URL of the elasticsearch service, including port.
   */
  @edu.kit.datamanager.annotations.ElasticsearchURL
  @Value("${metastore.indexer.elastic.baseUrl:http://localhost:9200}")
  URL elasticsearchUrl;

  /**
   * The elastic index ("database") where the records will be stored into.
   */
  @edu.kit.datamanager.annotations.ElasticsearchIndex
  @Value("${metastore.indexer.elastic.index:kitdm}")
  String elasticsearchIndex;

  /**
   * The elasticsearch type of the document.
   */
  @edu.kit.datamanager.annotations.ElasticsearchIndex
  @Value("${metastore.indexer.elastic.type:_doc}")
  String elasticsearchType;

}
