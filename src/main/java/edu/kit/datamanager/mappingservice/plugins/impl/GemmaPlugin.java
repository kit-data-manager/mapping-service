/*
 * Copyright 2023 Karlsruhe Institute of Technology.
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

import edu.kit.datamanager.mappingservice.plugins.*;
import edu.kit.datamanager.mappingservice.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import java.nio.file.Path;

public class GemmaPlugin implements IMappingPlugin {

    private final Logger LOGGER = LoggerFactory.getLogger(GemmaPlugin.class);
    private static final String GEMMA_REPOSITORY = "https://github.com/kit-data-manager/gemma.git";
    private static final String GEMMA_BRANCH = "master";
    private static Path gemmaDir;
    private boolean initialized = false;

    @Override
    public String name() {
        return "GEMMA";
    }

    @Override
    public String description() {
        return "GEMMA is a tool written in Python that allows to map from JSON and XML to JSON. Furthermore, it allows to map with a mapping schema.";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String uri() {
        return "https://github.com/kit-data-manager/gemma";
    }

    @Override
    public MimeType[] inputTypes() {
        return new MimeType[]{MimeTypeUtils.APPLICATION_JSON, MimeTypeUtils.APPLICATION_XML};
    }

    @Override
    public MimeType[] outputTypes() {
        return new MimeType[]{MimeTypeUtils.APPLICATION_JSON};
    }

    @Override
    public void setup() {
        LOGGER.info("Checking and installing dependencies for Gemma: gemma, xmltodict, wget");
        try {
            //PythonRunnerUtil.runPythonScript("-m", "pip", "install", "xmltodict", "wget");
            PythonRunnerUtil.runPythonScript("-m", new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.DEBUG), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.DEBUG), "pip", "install", "xmltodict", "wget");
            gemmaDir = FileUtil.cloneGitRepository(GEMMA_REPOSITORY, GEMMA_BRANCH);
            initialized = true;
        } catch (MappingPluginException e) {
            LOGGER.error("Failed to setup plugin '" + name() + "' " + version() + ".", e);
        }
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        if (initialized) {
            LOGGER.trace("Run gemma on '{}' with mapping '{}' -> '{}'", inputFile, mappingFile, outputFile);
            return PythonRunnerUtil.runPythonScript(gemmaDir + "/mapping_single.py", mappingFile.toString(), inputFile.toString(), outputFile.toString());
        } else {
            LOGGER.error("Plugin '" + name() + "' " + version() + " not initialized. Returning EXECUTION_ERROR.");
            MappingPluginState result = MappingPluginState.EXECUTION_ERROR();
            result.setDetails("Plugin not initialized, probably due to missing dependencies or external plugin repository.");
            return result;
        }
    }
}
