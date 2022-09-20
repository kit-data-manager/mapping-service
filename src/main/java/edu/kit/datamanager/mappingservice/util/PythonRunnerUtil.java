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

/**
 * Utility class for running python scripts.
 *
 * @author maximilianiKIT
 */
@Component
public class PythonRunnerUtil {
    private static ApplicationProperties configuration;

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonRunnerUtil.class);

    @Autowired
    public PythonRunnerUtil(ApplicationProperties configuration) {
        PythonRunnerUtil.configuration = configuration;
    }

    /**
     * This method prints the python version to Log.info.
     */
    public static void printPythonVersion() {
        try {
            LOGGER.info("Configured Python version:");
            PythonRunnerUtil.runPythonScript("--version");
        } catch (MappingPluginException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method executes an argument/option on the python interpreter.
     *
     * @param arg single argument/option to be executed.
     * @return State of the execution.
     * @throws MappingPluginException if an error occurs.
     */
    public static MappingPluginState runPythonScript(String arg) throws MappingPluginException {
        return runPythonScript(arg, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.WARN));
    }

    /**
     * This method executes a python script with the given arguments.
     *
     * @param script path to the python script to be executed.
     * @param args   arguments to be passed to the script.
     * @return State of the execution.
     * @throws MappingPluginException if an error occurs.
     */
    public static MappingPluginState runPythonScript(String script, String... args) throws MappingPluginException {
        return runPythonScript(script, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.WARN), args);
    }

    /**
     * This method executes a python script with the given arguments and redirects the output and errors to the given streams.
     *
     * @param script path to the python script to be executed.
     * @param output OutputStream to redirect the output to.
     * @param error  OutputStream to redirect the errors to.
     * @param args   arguments to be passed to the script.
     * @return State of the execution.
     * @throws MappingPluginException if an error occurs.
     */
    public static MappingPluginState runPythonScript(String script, OutputStream output, OutputStream error, String... args) throws MappingPluginException {
        if (configuration == null || configuration.getPythonLocation() == null) return MappingPluginState.UNKNOWN_ERROR;
        ArrayList<String> command = new ArrayList<>();
        command.add(configuration.getPythonLocation().getPath());
        command.add(script);
        Collections.addAll(command, args);
        ShellRunnerUtil.run(output, error, command.toArray(new String[0]));
        return MappingPluginState.SUCCESS;
    }
}
