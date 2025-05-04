/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.kit.datamanager.mappingservice.plugins;

import edu.kit.datamanager.mappingservice.exception.PluginInitializationFailedException;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import edu.kit.datamanager.mappingservice.util.ShellRunnerUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public abstract class AbstractPythonMappingPlugin implements IMappingPlugin {

    private final Logger LOGGER = LoggerFactory.getLogger(AbstractPythonMappingPlugin.class);
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
     * Abstract method that is supposed to be implemented by each Python mapping
     * plugin the gather all information required for starting a Python process
     * executing the mapping script. The returned array should contain at least
     * the following information:
     *
     * &lt;ul&gt; &lt;li&gt;The absolute path of the main script. It must start
     * with the working dir given as argument, where all checked out code is
     * located.&lt;/li&gt; &lt;li&gt;Script-specific parameters to provide
     * mappingFile, inputFile, and outputFile. Depending on the script
     * implementation the number of required arguments may differ.&lt;/li&gt;
     * &lt;/ul&gt;
     *
     * Example: In standalone mode, your script is called via `plugin_wrapper.py
     * sem -m mappingFile -i inputFile -o outputFile`. In that case, the
     * resulting array should look as follows: [workingDir +
     * "plugin_wrapper.py", "sem", "-m", mappingFile.toString(), "-i",
     * inputFile.toString(), "-o", outputFile.toString()]. The Python call
     * itself will be added depending on the local installation and must not be
     * included.
     *
     * @param workingDir The working directory, i.e., where the plugin code was
     * checked out into.
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
        
        
        
        
        try {
            LOGGER.info("Cloning git repository {}, Tag {}", repositoryUrl, tag);
            dir = FileUtil.cloneGitRepository(repositoryUrl, tag);
            // Install Python dependencies
            MappingPluginState venvState = PythonRunnerUtil.runPythonScript("-m", "venv", "--system-site-packages", dir + "/" + pluginVenv);
            if (MappingPluginState.SUCCESS().getState().equals(venvState.getState())) {
                LOGGER.info("Venv for plugin installed successfully. Installing packages.");
                ShellRunnerUtil.run(dir + "/" + venvInterpreter, "-m", "pip", "install", "-r", dir + "/" + "requirements.dist.txt");
            } else {
                throw new PluginInitializationFailedException("Venv installation was not successful. Status: " + venvState.getState());
            }
        } catch (MappingPluginException e) {
            throw new PluginInitializationFailedException("Unexpected error during plugin setup.", e);
        }
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        long startTime = System.currentTimeMillis();
        LOGGER.trace("Run SEM-Mapping-Tool on '{}' with mapping '{}' -> '{}'", mappingFile, inputFile, outputFile);
        String[] commandArray = getCommandArray(dir, mappingFile, inputFile, outputFile);
        List<String> command = new LinkedList<>();
        command.add(dir + "/" + venvInterpreter);
        command.addAll(Arrays.asList(commandArray));
        //MappingPluginState result = ShellRunnerUtil.run(dir + "/" + venvInterpreter, dir + "/plugin_wrapper.py", "sem", "-m", mappingFile.toString(), "-i", inputFile.toString(), "-o", outputFile.toString());
        MappingPluginState result = ShellRunnerUtil.run(command.toArray(String[]::new));
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        LOGGER.info("Execution time of mapFile: {} milliseconds", totalTime);
        return result;
    }
}
