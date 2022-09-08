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
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

@Component
public class PythonRunnerUtil {
    //    private static GemmaConfiguration gemmaConfiguration;
    private static ApplicationProperties configuration;

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonRunnerUtil.class);

    @Autowired
    public PythonRunnerUtil(ApplicationProperties configuration) {
        PythonRunnerUtil.configuration = configuration;
    }
//    @Autowired
//    public PythonRunnerUtil(ApplicationProperties configuration) throws MalformedURLException {
//        gemmaConfiguration = new GemmaConfiguration();
//        File gemmaFile = new File(configuration.getGemmaLocation().getPath());
//        File pythonExecutable = new File(configuration.getPythonLocation().getPath());
//        gemmaConfiguration.setGemmaLocation(gemmaFile.toURI().toURL());
//        gemmaConfiguration.setPythonLocation(pythonExecutable.toURI().toURL());
//    }

    public static void printPythonVersion() {
        try {
            ShellRunnerUtil.run(new String[]{configuration.getPythonLocation().getPath(), "--version"}, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.WARN));
        } catch (MappingPluginException e) {
            e.printStackTrace();
        }
    }


    public static MappingPluginState runPythonScript(String arg) throws MappingPluginException {
        return runPythonScript(arg, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.WARN));
    }

    public static MappingPluginState runPythonScript(String script, String... args) throws MappingPluginException {
        return runPythonScript(script, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.WARN), args);
    }

    public static MappingPluginState runPythonScript(String script, OutputStream output, OutputStream error, String... args) throws MappingPluginException {
        ArrayList<String> command = new ArrayList<>();
        command.add(configuration.getPythonLocation().getPath());
        command.add(script);
        Collections.addAll(command, args);
        ShellRunnerUtil.run(command.toArray(new String[]{}), output, error);
        return MappingPluginState.SUCCESS;
    }
}
