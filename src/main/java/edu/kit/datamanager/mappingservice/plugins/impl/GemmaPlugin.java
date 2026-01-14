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

import edu.kit.datamanager.mappingservice.plugins.AbstractPythonMappingPlugin;

import java.nio.file.Path;

public class GemmaPlugin extends AbstractPythonMappingPlugin {

    private static final String GEMMA_REPOSITORY = "https://github.com/kit-data-manager/gemma.git";

    public GemmaPlugin() {
        super("GEMMA", GEMMA_REPOSITORY);
    }

    @Override
    public String description() {
        return "GEMMA is a tool written in Python that allows to map from JSON and XML to JSON. Furthermore, it allows to map with a mapping schema.";
    }

    @Override
    public String[] inputTypes() {
        return new String[]{"application/json", "application/xml"};
    }

    @Override
    public String[] outputTypes() {
        return new String[]{"application/json"};
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
}
