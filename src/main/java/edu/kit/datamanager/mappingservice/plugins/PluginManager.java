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

public class PluginManager {
    static Logger LOG = LoggerFactory.getLogger(IMappingPlugin.class);
    private static final PluginManager soleInstance = new PluginManager();

    private static Map<String, IMappingPlugin> plugins;

    static {
        reloadPlugins();
    }

    private PluginManager() {
        // enforces singularity
    }

    public static PluginManager soleInstance() {
        return soleInstance;
    }

    public static void reloadPlugins(){
        Map<String, IMappingPlugin> plugins1;
        try {
            plugins1 = PluginLoader.loadPlugins(new File("./plugins"));
        } catch (Exception e) {
            LOG.info("No plugins loaded.", e);
            plugins1 = new HashMap<>();
        }
        plugins = plugins1;
    }

    public Map getPlugins() {
        return plugins;
    }

    public List<String> getListOfAvailableValidators() {
        Map<String, IMappingPlugin> map = PluginManager.soleInstance().getPlugins();
        List<String> result = new ArrayList<>();
        for (var entry : map.entrySet()) {
            result.add(entry.getKey().toString());
        }
        return result;
    }

    public MappingPluginStates mapFile(String pluginId, Path mappingFile, Path inputFile, Path outputFile) throws MappingPluginException {
        for (var entry : plugins.entrySet()) {
            if (entry.getKey().toString().equals(pluginId)) {
                return entry.getValue().mapFile(mappingFile, inputFile, outputFile);
            }
        }
        throw new MappingPluginException(MappingPluginStates.NOT_FOUND, "Plugin not found!");
    }

    public static void main(String[] args) {
        for (var entry : plugins.entrySet()) {
            System.out.println(entry.getValue().id().toString());
            System.out.println(entry.getValue().id().toString());
        }
    }
}
