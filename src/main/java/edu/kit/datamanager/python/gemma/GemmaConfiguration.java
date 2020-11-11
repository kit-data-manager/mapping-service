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

import edu.kit.datamanager.configuration.GenericPluginProperties;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jejkal
 */
@ConfigurationProperties(prefix = "repo.plugin.gemma")
@Configuration
@Data
public class GemmaConfiguration extends GenericPluginProperties{

  private Map<String, String> schemaMappings;

  private String mappingsLocation;

  private String pythonLocation;

  private String gemmaLocation;

}
