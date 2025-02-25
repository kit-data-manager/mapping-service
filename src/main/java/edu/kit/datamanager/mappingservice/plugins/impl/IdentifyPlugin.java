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

import edu.kit.datamanager.mappingservice.plugins.IMappingPlugin;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.util.ShellRunnerUtil;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;

/**
 * Simple mapping service plugin calling image magick 'identify' to obtain image
 * metadata from a given image.
 *
 * @author jejkal
 */
public class IdentifyPlugin implements IMappingPlugin {

    static Logger LOG = LoggerFactory.getLogger(IdentifyPlugin.class);

    private boolean initialized = false;

    @Override
    public String name() {
        return "Identify";
    }

    @Override
    public String description() {
        return "Simple mapping service plugin calling image magick 'identify' to obtain image metadata from a given image.";
    }

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public String uri() {
        return "https://github.com/kit-data-manager/mapping-service";
    }

    @Override
    public MimeType[] inputTypes() {
        return new MimeType[]{MimeType.valueOf("image/*")};
    }

    @Override
    public MimeType[] outputTypes() {
        return new MimeType[]{MimeType.valueOf("application/*")};
    }

    @Override
    public void setup() {
        if (Paths.get("/usr/bin/identify").toFile().exists()) {
            initialized = true;
        }
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        try {
            FileOutputStream fout = new FileOutputStream(outputFile.toFile());
            ShellRunnerUtil.run(fout, fout, "/usr/bin/identify", "-verbose", inputFile.toAbsolutePath().toString());
            fout.flush();
            fout.close();
        } catch (IOException ex) {
            LOG.error("Failed to execute plugin.", ex);
            return MappingPluginState.EXECUTION_ERROR;
        }
        return MappingPluginState.SUCCESS;
    }

}
