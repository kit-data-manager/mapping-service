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

import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.plugins.IMappingPlugin;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;

/**
 *
 * @author jejkal
 */
public class InOutPlugin implements IMappingPlugin {

    static Logger LOG = LoggerFactory.getLogger(InOutPlugin.class);

    @Override
    public String name() {
        return "InOutPlugin";
    }

    @Override
    public String description() {
        return "Simple plugin for testing just returning the input file.";
    }

    @Override
    public String version() {
        return "1.1.2";
    }

    @Override
    public String uri() {
        return "https://github.com/kit-data-manager/mapping-service";
    }

    @Override
    public MimeType[] inputTypes() {
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public MimeType[] outputTypes() {
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public void setup() {
        //nothing to do here
        LOG.trace("Plugin {} {} successfully set up.", name(), version());
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        try {
            Files.copy(inputFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | MappingException ex) {
            LOG.error("Failed to execute plugin.", ex);
            return MappingPluginState.EXECUTION_ERROR();
        }
        return MappingPluginState.SUCCESS();
    }

}
