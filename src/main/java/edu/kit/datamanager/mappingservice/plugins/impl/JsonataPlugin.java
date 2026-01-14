/*
 * Copyright 2026 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.mappingservice.plugins.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.plugins.IMappingPlugin;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dashjoin.jsonata.Jsonata;

/**
 * Plugin implementation of the Jsonata query and transformation language for JSON data. For more information and
 * format description please check the [JSONata Homepage](https://docs.jsonata.org/overview.html).
 *
 * @author jejkal
 */
public class JsonataPlugin implements IMappingPlugin {
    static Logger LOG = LoggerFactory.getLogger(JsonataPlugin.class);

    @Override
    public String name() {
        return "JsonataPlugin";
    }

    @Override
    public String description() {
        return "Plugin for Jsonata-based JSON to JSON transformation.";
    }

    @Override
    public String version() {
        return "2.0.0";
    }

    @Override
    public String uri() {
        return "https://github.com/kit-data-manager/mapping-service";
    }

    @Override
    public String[] inputTypes() {
        return new String[]{"application/json"};
    }

    @Override
    public String[] outputTypes() {
        return new String[]{"application/json", "text/plain"};
    }

    @Override
    public void setup(ApplicationProperties applicationProperties) {
        //nothing to do here
        LOG.trace("Plugin {} {} successfully set up.", name(), version());
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        MappingPluginState result = MappingPluginState.SUCCESS();
        try {
            String mappingContent = Files.readString(mappingFile);
            Jsonata expression = Jsonata.jsonata(mappingContent);
            ObjectMapper mapper = new ObjectMapper();
            // Load the input JSON
            Map<String, Object> inputJson = mapper.readValue(inputFile.toFile(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            expression.evaluate(inputJson);

            // Apply transformation
            Object transformedOutput = expression.evaluate(inputJson);

            // Print result
            String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformedOutput);

            try (FileWriter writer = new FileWriter(outputFile.toFile())) {
                writer.write(output);
            }
        } catch (IOException | MappingException ex) {
            LOG.error("Failed to execute plugin.", ex);
            result = MappingPluginState.EXECUTION_ERROR();
            result.setDetails("Failed to run Jsonata transformation.");
        }
        return result;
    }

}
