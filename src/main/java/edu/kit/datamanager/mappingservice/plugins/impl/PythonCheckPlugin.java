package edu.kit.datamanager.mappingservice.plugins.impl;

import com.google.common.io.Files;
import edu.kit.datamanager.mappingservice.plugins.*;
import edu.kit.datamanager.mappingservice.util.*;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.apache.commons.io.FileUtils;

public class PythonCheckPlugin implements IMappingPlugin {

    private static String version;

    private final Logger LOGGER = LoggerFactory.getLogger(PythonCheckPlugin.class);
    private final String REPOSITORY = "https://github.com/kit-data-manager/mapping-service";
    private String TAG;
    private Path dir;
    private String pluginVenv = "venv/PluginVenv";
    private String venvInterpreter;

    public PythonCheckPlugin() {
        try {
            // Get the context class loader
            ClassLoader classLoader = this.getClass().getClassLoader();
            Properties props = new Properties();
            props.load(classLoader.getResourceAsStream("META-INF/build-info.properties"));

            version = props.getProperty("build.version");

            // TODO: do we need to make sure that the resource path is somehow related to the current plugin to avoid loading the wrong property file in case of identical property names?
            /*URL resource = classLoader.getResource("sempluginversion.properties");
            LOGGER.info("Resource file: {}", resource);
            if (resource != null) {
                // Load the properties file
                try (InputStream input = resource.openStream()) {
                    Properties properties = new Properties();
                    properties.load(input);
                    version = properties.getProperty("version");
                    TAG = version;
                }
            } else {
                System.err.println("Properties file not found!");
                version = "unavailable";
                TAG = "unavailable";
            }*/
            if (System.getProperty("os.name").startsWith("Windows")) {
                venvInterpreter = pluginVenv + "/Scripts/python.exe";
            } else {
                venvInterpreter = pluginVenv + "/bin/python3";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String name() {
        return "PythonCheckPlugin";
    }

    @Override
    public String description() {
        return "This is a sample plugin that can be used to check the python version used by this mapping-service instance.";
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public String uri() {
        return REPOSITORY;
    }

    @Override
    public MimeType[] inputTypes() {
        return new MimeType[]{MimeTypeUtils.parseMimeType("plain/text")}; //should currently be IMAGE/TIFF
    }

    @Override
    public MimeType[] outputTypes() {
        return new MimeType[]{MimeTypeUtils.parseMimeType("plain/text")};
    }

    @Override
    public void setup() {
        LOGGER.info("Checking and installing dependencies for the tool: ");
        //TODO: test for minimal python version?
        try {
            //LOGGER.info("Cloning git repository {}, Tag {}", REPOSITORY, TAG);
            //dir = FileUtil.cloneGitRepository(REPOSITORY, TAG);
            // Install Python dependencies

            Path p = Paths.get("tmp", name(), version());
            if (!p.toFile().exists()) {
                FileUtils.forceMkdir(p.toFile());
            }

            
            
            
            
            
            MappingPluginState venvState = PythonRunnerUtil.runPythonScript("-m", "venv", "--system-site-packages", p.toAbsolutePath().toString() + "/" + pluginVenv);
            if (venvState.getState() == MappingPluginState.StateEnum.SUCCESS) {
                LOGGER.info("Venv for plugin installed succesfully.");
                LOGGER.info("Installing packages");
                ShellRunnerUtil.run(p.toAbsolutePath().toString() + "/" + venvInterpreter, "-m", "pip", "install", "-r", p.toAbsolutePath().toString() + "/" + pluginVenv + "/requirements.dist.txt");
            } else {
                LOGGER.error("venv installation was not successful");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        long startTime = System.currentTimeMillis();
        LOGGER.trace("Run SEM-Mapping-Tool on '{}' with mapping '{}' -> '{}'", mappingFile, inputFile, outputFile);
        MappingPluginState result = ShellRunnerUtil.run(dir + "/" + venvInterpreter, dir + "/plugin_wrapper.py", "sem", "-m", mappingFile.toString(), "-i", inputFile.toString(), "-o", outputFile.toString());
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        LOGGER.info("Execution time of mapFile: {} milliseconds", totalTime);
        return result;
    }
}
