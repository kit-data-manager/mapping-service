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
package edu.kit.datamanager.mappingservice.plugins;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for managing plugins and their execution.
 *
 * @author maximilianiKIT
 */
@Component
public class PluginManager {

    /**
     * Logger for this class.
     */
    static Logger LOG = LoggerFactory.getLogger(IMappingPlugin.class);

    /**
     * Application properties autowired at instantiation time.
     */
    private final ApplicationProperties applicationProperties;

    /**
     * Map of plugins.
     */
    private Map<String, IMappingPlugin> plugins = new HashMap<>();

    /**
     * Constructor with autowired applicationProperties.
     *
     * @param applicationProperties Application properties autowired at
     * instantiation time.
     */
    @Autowired
    public PluginManager(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        reloadPlugins();
    }

    /**
     * Unload all plugins and reload them from the configured plugin folder.
     */
    public final void unload() {
        PluginLoader.unload();
        plugins.clear();
    }

    /**
     * Reloads the plugins from the 'plugins' directory.
     */
    public final void reloadPlugins() {
        unload();
        try {
            plugins = PluginLoader.loadPlugins(Paths.get(applicationProperties.getPluginLocation().toURI()).toFile());
        } catch (URISyntaxException ex) {
            LOG.error("Mapping plugin location " + applicationProperties.getPluginLocation() + " cannot be converted to URI", ex);
        } catch (IOException ioe) {
            LOG.error("Failed to open plugin libraries at plugin location " + applicationProperties.getPluginLocation() + ".", ioe);
        } catch (MappingPluginException e) {
            LOG.info("Unable to obtain plugin classes from libraries at plugin location " + applicationProperties.getPluginLocation() + ".", e);
        }
    }

    /**
     * Gets the map of plugins. The key is the plugin id.
     *
     * @return map of plugins
     */
    public final Map<String, IMappingPlugin> getPlugins() {
        return plugins;
    }

    /**
     * Gets a list of all plugin ids.
     *
     * @return List of plugin ids
     */
    public final List<String> listPluginIds() {
        Map<String, IMappingPlugin> map = getPlugins();
        List<String> result = new ArrayList<>();
        map.entrySet().forEach(entry -> {
            result.add(entry.getKey());
        });
        return result;
    }

    /**
     * Executes a mapping on a plugin.
     *
     * @param pluginId ID of the plugin to execute.
     * @param mappingFile Path to the mapping schema.
     * @param inputFile Path to the input file.
     * @param outputFile Path where the output is temporarily stored.
     * 
     * @return MappingPluginState.SUCCESS if the plugin was executed
     * successfully.
     * 
     * @throws MappingPluginException If there is an error with the plugin or
     * the input.
     */
    public final MappingPluginState mapFile(String pluginId, Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        if (pluginId == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Plugin ID is null.");
        }
        if (mappingFile == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Path to mapping schema is null.");
        }
        if (inputFile == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Path to input file is null.");
        }
        if (outputFile == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Path to output file is null.");
        }

        if (plugins.containsKey(pluginId)) {
            LOG.trace("Plugin found. Performing mapFile({}, {}, {}).", mappingFile, inputFile, outputFile);
            return plugins.get(pluginId).mapFile(mappingFile, inputFile, outputFile);
        }
        throw new MappingPluginException(MappingPluginState.NOT_FOUND, "Plugin '" + pluginId + "' not found!");
    }
}
