package edu.kit.datamanager.mappingservice.plugins.impl;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.plugins.IMappingPlugin;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JoltPlugin implements IMappingPlugin {
    static Logger LOG = LoggerFactory.getLogger(JoltPlugin.class);

    @Override
    public String name() {
        return "JoltPlugin";
    }

    @Override
    public String description() {
        return "Plugin for Jolt-based JSON to JSON transformation.";
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
    public String[] inputTypes() {
        return new String[]{"application/json"};
    }

    @Override
    public String[] outputTypes() {
        return new String[]{"application/json"};
    }

    @Override
    public void setup(ApplicationProperties applicationProperties) {
        //nothing to do here
        LOG.trace("Plugin {} {} successfully set up.", name(), version());
    }

    @Override
    public MappingPluginState mapFile(Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        MappingPluginState result = MappingPluginState.SUCCESS();
        try {
            Files.copy(inputFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | MappingException ex) {
            LOG.error("Failed to execute plugin.", ex);
            result = MappingPluginState.EXECUTION_ERROR();
            result.setDetails("Failed to copy input to output, probably due to an I/O error.");
        }
        return result;
    }

}


