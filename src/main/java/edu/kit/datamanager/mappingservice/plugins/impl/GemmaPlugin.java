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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import java.nio.file.Path;

public class GemmaPlugin extends AbstractPythonMappingPlugin {

    private final Logger LOGGER = LoggerFactory.getLogger(GemmaPlugin.class);
    private static final String GEMMA_REPOSITORY = "https://github.com/kit-data-manager/gemma.git";
    private static final String GEMMA_BRANCH = "master";
    private static Path gemmaDir;
    private boolean initialized = false;

    public GemmaPlugin() {
        super("GEMMA", GEMMA_REPOSITORY);
    }

    @Override
    public String description() {
        return "GEMMA is a tool written in Python that allows to map from JSON and XML to JSON. Furthermore, it allows to map with a mapping schema.";
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
    public String[] getCommandArray(Path workingDir, Path mappingFile, Path inputFile, Path outputFile) {
        return new String[]{
            workingDir + "/mapping_single.py",
            mappingFile.toString(),
            inputFile.toString(),
            outputFile.toString()
        };
    }

  /*  @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        if (initialized) {
            LOGGER.trace("Running plugin '{}' v{} on '{}' with mapping '{}' -> '{}'", name(), version(), inputFile, mappingFile, outputFile);
            return PythonRunnerUtil.runPythonScript(gemmaDir + "/mapping_single.py", mappingFile.toString(), inputFile.toString(), outputFile.toString());
        } else {
            LOGGER.error("Plugin '" + name() + "' " + version() + " not initialized. Returning EXECUTION_ERROR.");
            MappingPluginState result = MappingPluginState.EXECUTION_ERROR();
            result.setDetails("Plugin not initialized, probably due to missing dependencies or external plugin repository.");
            return result;
        }
    }*/
}
