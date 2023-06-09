/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
public class TestPlugin implements IMappingPlugin {

    static Logger LOG = LoggerFactory.getLogger(TestPlugin.class);

    @Override
    public String name() {
        return "TestPlugin";
    }

    @Override
    public String description() {
        return "Simple plugin for testing.";
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
        return new MimeType[]{MimeType.valueOf("application/octet-stream")};
    }

    @Override
    public MimeType[] outputTypes() {
        return new MimeType[]{MimeType.valueOf("application/octet-stream")};
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
            return MappingPluginState.EXECUTION_ERROR;
        }
        return MappingPluginState.SUCCESS;
    }

}
