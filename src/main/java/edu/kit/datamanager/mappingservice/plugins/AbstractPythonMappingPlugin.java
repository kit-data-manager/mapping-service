/*
 * Copyright 2025 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.mappingservice.plugins;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.PluginInitializationFailedException;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import edu.kit.datamanager.mappingservice.util.ShellRunnerUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author jejkal
 */
public abstract class AbstractPythonMappingPlugin implements IMappingPlugin {

    private final Logger LOGGER = LoggerFactory.getLogger(AbstractPythonMappingPlugin.class);

    /**
     * Application properties autowired at instantiation time via
     * setApplicationProperties.
     */
    private ApplicationProperties applicationProperties;
    /**
     * The plugin name.
     */
    private String name;
    /**
     * The URL of the Git repository where the plugin code is located.
     */
    private String repositoryUrl;

    /**
     * The tag which should be used to checkout a specific version from
     * repositoryUrl.
     */
    private String tag;

    /**
     * The minimal python version required by the plugin
     */
    private String minPython;

    /**
     * The folder where the code is checked out from repositoryUrl.
     */
    private Path dir;

    private String pluginVenv = "venv/PluginVenv";
    private String venvInterpreter;

    /**
     * Default constructor for instantiating a Python-based mapping plugin. It
     * is assumed, that the code for the plugin is stored in a Git repository
     * located at 'repositoryUrl'. Furthermore, it is assumed, that the plugin
     * itself is delivered as a single Jar file which contains at least the
     * plugin class and a file PLUGIN_NAME.properties, where PLUGIN_NAME must be
     * identical to the 'pluginName' argument and the file must be located in
     * the root of the Jar file. The properties file must contain one property
     * 'version' which represents an existing Git tag matching a released
     * version of the Python plugin code, e.g., version=v1.0.0
     *
     * Furthermore, the properties file may contain a minimal Python version
     * that is required by the plugin to work. The minimal Python version is set
     * via the 'min.python' property, e.g.., min.python=3.10.0 If the minimal
     * Python version is not met, the plugin will be ignored.
     *
     * @param pluginName The name of the plugin.
     * @param repositoryUrl The Git repository where the plugin Python code is
     * located.
     */
    public AbstractPythonMappingPlugin(String pluginName, String repositoryUrl) {
        try {
            name = pluginName;
            this.repositoryUrl = repositoryUrl;
            // Get the context class loader
            ClassLoader classLoader = this.getClass().getClassLoader();
            // TODO: do we need to make sure that the resource path is somehow related to the current plugin to avoid loading the wrong property file in case of identical property names?
            URL resource = classLoader.getResource(pluginName.toLowerCase() + ".properties");
            LOGGER.info("Resource file: {}", resource);
            if (resource != null) {
                // Load the properties file
                try (InputStream input = resource.openStream()) {
                    Properties properties = new Properties();
                    properties.load(input);
                    tag = properties.getProperty("version");
                    minPython = properties.getProperty("min.python");
                }
            } else {
                System.err.println("Properties file not found!");
                tag = "unavailable";
            }

            if (System.getProperty("os.name").startsWith("Windows")) {
                venvInterpreter = pluginVenv + "/Scripts/python.exe";
            } else {
                venvInterpreter = pluginVenv + "/bin/python3";
            }
        } catch (IOException e) {
            throw new PluginInitializationFailedException("Failed to instantiate plugin class.", e);
        }
    }

    /**
     * Setter to autowire ApplicationProperties into all implementations of this
     * abstract class.
     *
     * @param applicationProperties The applicationProperties bean.
     */
    @Autowired
    public final void setApplicationProperties(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Abstract method that is supposed to be implemented by each Python mapping
     * plugin to gather all information required for starting a Python process
     * executing the mapping script. The returned array must contain at least
     * the following information:
     *
     * &lt;ul&gt; &lt;li&gt;The absolute path of the main script. It must start
     * with the working dir received as argument, where all checked-out code is
     * located.&lt;/li&gt; &lt;li&gt;Script-specific parameters to provide
     * mappingFile, inputFile, and outputFile to the script execution. Depending
     * on the script implementation, the number and kind of required arguments
     * may differ.&lt;/li&gt; &lt;/ul&gt;
     *
     * Example: In standalone mode, a script is called via `plugin_wrapper.py
     * sem -m mappingFile -i inputFile -o outputFile -debug`. In that case, the
     * resulting array should look as follows: [workingDir +
     * "plugin_wrapper.py", "sem", "-m", mappingFile.toString(), "-i",
     * inputFile.toString(), "-o", outputFile.toString(), "-debug"].
     *
     * The Python call itself will be added according to the Venv used for
     * plugin execution and must not be included.
     *
     * @param workingDir The working directory, i.e., where the plugin code was
     * checked-out into.
     * @param mappingFile The file which contains the mapping rules registered
     * at the mapping-service and used by the script.
     * @param inputFile The file which was uploaded by the user, i.e., the
     * source of the mapping process.
     * @param outputFile The destination where mapping results must be written
     * to in order to allow the mapping-service to return the result to the
     * user.
     *
     * @return A string array containing the single elements of the command line
     * call of the script.
     */
    public abstract String[] getCommandArray(Path workingDir, Path mappingFile, Path inputFile, Path outputFile);

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String version() {
        return this.tag;
    }

    @Override
    public String description() {
        return "Plugin " + name() + ", Version " + version() + ", Implementation: " + uri();
    }

    @Override
    public String uri() {
        return this.repositoryUrl;
    }

    @Override
    public void setup() {
        LOGGER.trace("Setting up mapping plugin {} {}", name(), version());

        //testing minimal Python version
        if (minPython != null) {
            if (!hasMinimalPythonVersion(minPython)) {
                throw new PluginInitializationFailedException("Minimal Python version '" + minPython + "' required by plugin not met.");
            }
        }

        //checkout and install plugin
        try {
            LOGGER.info("Cloning git repository {}, tag {}", repositoryUrl, tag);
            dir = FileUtil.cloneGitRepository(repositoryUrl, tag, Paths.get(applicationProperties.getCodeLocation().toURI()).toAbsolutePath().toString());
            // Install Python dependencies
            MappingPluginState venvState = PythonRunnerUtil.runPythonScript("-m", "venv", "--system-site-packages", dir + "/" + pluginVenv);
            if (MappingPluginState.SUCCESS().getState().equals(venvState.getState())) {
                LOGGER.info("Venv for plugin installed successfully. Installing packages.");
                MappingPluginState requirementsInstallState = ShellRunnerUtil.run(dir + "/" + venvInterpreter, "-m", "pip", "install", "-r", dir + "/" + "requirements.dist.txt");
                if (MappingPluginState.SUCCESS().getState().equals(requirementsInstallState.getState())) {
                    LOGGER.info("Requirements for plugin installed successfully. Setup complete.");
                } else {
                    throw new PluginInitializationFailedException("Failed to install plugin requirements. Status: " + venvState.getState());
                }
            } else {
                throw new PluginInitializationFailedException("Venv installation has failed. Status: " + venvState.getState());
            }
        } catch (URISyntaxException e) {
            throw new PluginInitializationFailedException("Invalid codeLocation configured in application.properties.", e);
        } catch (MappingPluginException e) {
            throw new PluginInitializationFailedException("Unexpected error during plugin setup.", e);
        }
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        long startTime = System.currentTimeMillis();
        LOGGER.trace("Run mapping plugin {} {} on '{}' with mapping '{}' -> '{}'", name(), version(), mappingFile, inputFile, outputFile);
        String[] commandArray = getCommandArray(dir, mappingFile, inputFile, outputFile);
        List<String> command = new LinkedList<>();
        command.add(dir + "/" + venvInterpreter);
        command.addAll(Arrays.asList(commandArray));
        MappingPluginState result = ShellRunnerUtil.run(command.toArray(String[]::new));
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        LOGGER.info("Execution time of mapFile: {} milliseconds", totalTime);
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
     * @param versionString The semantic version string to compare the local
     * Python version against.
     *
     * @return True if versionString is smaller or equal the local Python
     * version, false otherwise.
     */
    private boolean hasMinimalPythonVersion(String versionString) {
        boolean result = false;
        try {
            LOGGER.trace("Checking for minimal Python version {}.", versionString);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            List<String> command = new LinkedList<>();
            command.add(dir + "/" + venvInterpreter);
            command.addAll(Arrays.asList("--version"));
            MappingPluginState state = ShellRunnerUtil.run(bout, System.err, command.toArray(String[]::new));

            if (!MappingPluginState.StateEnum.SUCCESS.equals(state.getState())) {
                LOGGER.error("Failed to obtain Python version. python --version returned with status {}.", state.getState());
            } else {

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
            }
        } catch (MappingPluginException e) {
            LOGGER.error("Failed to obtain Python version.", e);
        }
        return result;
    }
}
