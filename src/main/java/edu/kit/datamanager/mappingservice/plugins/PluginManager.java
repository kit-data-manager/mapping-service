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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for managing plugins and their execution.
 *
 * @author maximilianiKIT
 */
public class PluginManager {
    /**
     * Logger for this class.
     */
    static Logger LOG = LoggerFactory.getLogger(IMappingPlugin.class);

    /**
     * Singleton instance.
     */
    private static final PluginManager soleInstance = new PluginManager();

    /**
     * Map of plugins.
     */
    private Map<String, IMappingPlugin> plugins;

    static {
        soleInstance.reloadPlugins(); // loads plugins on startup
    }

    /**
     * Private constructor for singleton.
     * This enforces singularity.
     */
    private PluginManager() {
    }

    /**
     * Get the singleton instance of this class.
     *
     * @return singleton instance
     */
    public static PluginManager soleInstance() {
        return soleInstance;
    }

    /**
     * Reloads the plugins from the 'plugins' directory.
     */
    public void reloadPlugins() {
        Map<String, IMappingPlugin> plugins1;
        try {
            plugins1 = PluginLoader.loadPlugins(new File("./plugins"));
        } catch (Exception e) {
            LOG.info("No plugins loaded.", e);
            plugins1 = new HashMap<>();
        }
        plugins = plugins1;
    }


    /**
     * Gets the map of plugins.
     * The key is the plugin id.
     *
     * @return map of plugins
     */
    public Map<String, IMappingPlugin> getPlugins() {
        return plugins;
    }

    /**
     * Gets a list of all plugin ids.
     *
     * @return List of plugin ids
     */
    public List<String> getListOfAvailableValidators() {
        Map<String, IMappingPlugin> map = PluginManager.soleInstance().getPlugins();
        List<String> result = new ArrayList<>();
        for (var entry : map.entrySet()) {
            result.add(entry.getKey());
        }
        return result;
    }

    /**
     * Executes a mapping on a plugin.
     *
     * @param pluginId    ID of the plugin to execute.
     * @param mappingFile Path to the mapping schema.
     * @param inputFile   Path to the input file.
     * @param outputFile  Path where the output is temporarily stored.
     * @return MappingPluginState.SUCCESS if the plugin was executed successfully.
     * @throws MappingPluginException If there is an error with the plugin or the input.
     */
    public MappingPluginState mapFile(String pluginId, Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
//        for (var entry : plugins.entrySet()) {
//            if (entry.getKey().equals(pluginId)) {
//                return entry.getValue().mapFile(mappingFile, inputFile, outputFile);
//            }
//        }
        if (pluginId == null) throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Plugin ID is null.");
        if (mappingFile == null) throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Path to mapping schema is null.");
        if (inputFile == null) throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Path to input file is null.");
        if (outputFile == null) throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Path to output file is null.");

        if(plugins.containsKey(pluginId)) return plugins.get(pluginId).mapFile(mappingFile, inputFile, outputFile);
        throw new MappingPluginException(MappingPluginState.NOT_FOUND, "Plugin '" + pluginId + "' not found!");
    }
}
