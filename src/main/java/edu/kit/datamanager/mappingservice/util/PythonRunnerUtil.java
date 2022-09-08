/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.mappingservice.util;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.python.gemma.GemmaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;

public class PythonRunnerUtil {
    GemmaConfiguration gemmaConfiguration;

    Logger LOGGER = LoggerFactory.getLogger(PythonRunnerUtil.class);

    public PythonRunnerUtil(ApplicationProperties configuration) throws MalformedURLException {
        gemmaConfiguration = new GemmaConfiguration();
        File gemmaFile = new File(configuration.getGemmaLocation().getPath());
        File pythonExecutable = new File(configuration.getPythonLocation().getPath());
        gemmaConfiguration.setGemmaLocation(gemmaFile.toURI().toURL());
        gemmaConfiguration.setPythonLocation(pythonExecutable.toURI().toURL());
    }

    public void printPythonVersion() {
        try {
            ShellRunnerUtil.run(new String[]{gemmaConfiguration.getPythonLocation().getPath(), "--version"}, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.WARN));
        } catch (MappingPluginException e) {
            e.printStackTrace();
        }
    }

}
