package edu.kit.datamanager.mappingservice.plugins.impl;

import com.bazaarvoice.jolt.Chainr;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.plugins.IMappingPlugin;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
        return "2.0.0";
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
            ObjectMapper mapper = new ObjectMapper();

            // Load the input JSON
            Map<String, Object> inputJson = mapper.readValue(inputFile.toFile(), Map.class);

            // Load the Jolt spec (as a List of operations)
            List<Object> joltSpec = mapper.readValue(mappingFile.toFile(), List.class);

            // Create the transformer
            Chainr chainr = Chainr.fromSpec(joltSpec);

            // Apply transformation
            Object transformedOutput = chainr.transform(inputJson);

            // Print result
            String output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformedOutput);

            try (FileWriter writer = new FileWriter(outputFile.toFile())) {
                writer.write(output);
            }
        } catch (IOException | MappingException ex) {
            LOG.error("Failed to execute plugin.", ex);
            result = MappingPluginState.EXECUTION_ERROR();
            result.setDetails("Failed to run Jolt transformation.");
        }
        return result;
    }

}


