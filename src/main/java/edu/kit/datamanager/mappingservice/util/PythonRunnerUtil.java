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
import java.io.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.maven.artifact.versioning.ComparableVersion;

/**
 * Utility class for running python scripts.
 *
 * @author maximilianiKIT
 */
@Component
public class PythonRunnerUtil {

    private static ApplicationProperties configuration;

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonRunnerUtil.class);

    public static void init(ApplicationProperties configuration) {
        PythonRunnerUtil.configuration = configuration;
    }

    /**
     * This method prints the python version to Log.info.
     */
    public static void printPythonVersion() {
        try {
            PythonRunnerUtil.runPythonScript("--version", System.out, System.err);
        } catch (MappingPluginException e) {
            LOGGER.error("Failed to obtain python version.", e);
        }
    }

    
    
    
    /**
     * This method checks if the local Python installation version is larger or
     * equal the provided version number. The version should be provided as
     * semantic version number, i.e., 3.13.2
     *
     * The method will return TRUE if the minimal requirements are met and false
     * otherwise. False is also returned if obtaining/parsing the local python
     * version fails. for any reason.
     * 
     * @param versionString The semantic version string to compare the local Python version against.
     * 
     * @return True if versionString is smaller or equal the local Python version, false otherwise.
     */
    public static boolean hasMinimalPythonVersion(String versionString) {
        boolean result = false;
        try {
            LOGGER.trace("Checking for minimal Python version {}.", versionString);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PythonRunnerUtil.runPythonScript("--version", bout, System.err);
            LOGGER.trace("Version command output: {}", bout.toString());

            String[] split = bout.toString().split(" ");

            if (split.length == 2) {
                String localPythonVersion = bout.toString().split(" ")[1].trim();
                LOGGER.trace("Obtained local Python version: {}", localPythonVersion);
                ComparableVersion localVersion = new ComparableVersion(localPythonVersion);
                ComparableVersion minimalVersion = new ComparableVersion(versionString);
                result = minimalVersion.compareTo(localVersion) <= 0;
            } else {
                LOGGER.info("Unexpected Python version output. Unable to check for minimal version.");
            }
        } catch (MappingPluginException e) {
            LOGGER.error("Failed to obtain python version.", e);
        }
        return result;
    }
    
     /**
     * This method checks if the local Python installation version is larger or
     * equal the provided version number. The version should be provided as
     * semantic version number, i.e., 3.13.2
     *
     * The method will return TRUE if the minimal requirements are met and false
     * otherwise. False is also returned if obtaining/parsing the local python
     * version fails. for any reason.
     * 
     * @param versionString The semantic version string to compare the local Python version against.
     * 
     * @return True if versionString is smaller or equal the local Python version, false otherwise.
     */
    public static boolean hasMaximalPythonVersion(String versionString) {
        boolean result = false;
        try {
            LOGGER.trace("Checking for minimal Python version {}.", versionString);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PythonRunnerUtil.runPythonScript("--version", bout, System.err);
            LOGGER.trace("Version command output: {}", bout.toString());

            String[] split = bout.toString().split(" ");

            if (split.length == 2) {
                String localPythonVersion = bout.toString().split(" ")[1].trim();
                LOGGER.trace("Obtained local Python version: {}", localPythonVersion);
                ComparableVersion localVersion = new ComparableVersion(localPythonVersion);
                ComparableVersion minimalVersion = new ComparableVersion(versionString);
                result = minimalVersion.compareTo(localVersion) > 0;
            } else {
                LOGGER.info("Unexpected Python version output. Unable to check for minimal version.");
            }
        } catch (MappingPluginException e) {
            LOGGER.error("Failed to obtain python version.", e);
        }
        return result;
    }

    /**
     * This method executes an argument/option on the python interpreter.
     *
     * @param arg single argument/option to be executed.
     * @return State of the execution.
     * @throws MappingPluginException if an error occurs.
     */
    public static MappingPluginState runPythonScript(String arg) throws MappingPluginException {
        return runPythonScript(arg, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.DEBUG), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.INFO));
    }

    /**
     * This method executes a python script with the given arguments.
     *
     * @param script path to the python script to be executed.
     * @param args arguments to be passed to the script.
     * @return State of the execution.
     * @throws MappingPluginException if an error occurs.
     */
    public static MappingPluginState runPythonScript(String script, String... args) throws MappingPluginException {
        return runPythonScript(script, new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.DEBUG), new LoggerOutputStream(LOGGER, LoggerOutputStream.Level.ERROR), args);
    }

    /**
     * This method executes a python script with the given arguments and
     * redirects the output and errors to the given streams.
     *
     * @param script path to the python script to be executed.
     * @param output OutputStream to redirect the output to.
     * @param error OutputStream to redirect the errors to.
     * @param args arguments to be passed to the script.
     *
     * @return State of the execution if execution succeeds. Otherwise, a
     * MappingPluginException is thrown.
     *
     * @throws MappingPluginException if an error occurs.
     */
    public static MappingPluginState runPythonScript(String script, OutputStream output, OutputStream error, String... args) throws MappingPluginException {
        if (configuration == null || !configuration.isPythonAvailable()) {
            throw new MappingPluginException(MappingPluginState.UNKNOWN_ERROR(), "No Python runtime configured.");
        }
        ArrayList<String> command = new ArrayList<>();
        command.add(configuration.getPythonExecutable().getPath());
        command.add(script);
        if (args != null) {
            Collections.addAll(command, args);
        }
        return ShellRunnerUtil.run(output, error, command.toArray(String[]::new));
    }

    public static void main(String[] args) throws Exception {
        ArrayList<String> command = new ArrayList<>();
        command.add("/opt/homebrew/bin/python3");
        command.add("--version");
        if (args != null) {
            Collections.addAll(command, args);
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        ShellRunnerUtil.run(bout, System.err, command.toArray(String[]::new));
        System.out.println("VERS " + bout.toString());
        System.out.println(bout.toString().split(" ")[1]);
        ComparableVersion verr = new ComparableVersion("unknown");
        System.out.println("ER " + verr.getCanonical());
        ComparableVersion v = new ComparableVersion(bout.toString().split(" ")[1].trim());
        ComparableVersion vMin = new ComparableVersion("4");
        System.out.println(verr.compareTo(v));

    }
}
