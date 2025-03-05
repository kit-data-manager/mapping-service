/*
 * Copyright 2021 Karlsruhe Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.plugins;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PluginLoaderTest {

    @Autowired
    private PluginManager pluginManager;
    
    @Autowired
    private PluginLoader pluginLoader;

    @Autowired
    private ApplicationProperties applicationProperties;

    
    @BeforeEach
    void setUp() throws Exception {
        try {
            FileUtils.copyDirectory(Path.of("./plugins").toFile(), Path.of(applicationProperties.getPluginLocation().toURI()).toFile());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        pluginManager.reloadPlugins();
    }

    @Test
    void valid() {
        System.out.println("Test valid");
        Map<String, IMappingPlugin> plugins = null;
        try {
            plugins = pluginLoader.loadPlugins(Path.of(applicationProperties.getPluginLocation().toURI()).toFile(), applicationProperties.getPackagesToScan());
        } catch (Exception e) {
            fail(e);
        }
 
        try {
            assertEquals("InOutPlugin_1.1.2", plugins.get("InOutPlugin_1.1.2").id());
            assertEquals("InOutPlugin", plugins.get("InOutPlugin_1.1.2").name());
            assertEquals("Simple plugin for testing just returning the input file.", plugins.get("InOutPlugin_1.1.2").description());
            assertEquals("1.1.2", plugins.get("InOutPlugin_1.1.2").version());
            assertEquals("https://github.com/kit-data-manager/mapping-service", plugins.get("InOutPlugin_1.1.2").uri());
            assertEquals("application/*", plugins.get("InOutPlugin_1.1.2").inputTypes()[0].toString());
            assertEquals("application/*", plugins.get("InOutPlugin_1.1.2").outputTypes()[0].toString());
            plugins.get("InOutPlugin_1.1.2").setup();
            File inputFile = new File("/tmp/imputFile");
            if (!inputFile.exists()) {
                Assertions.assertTrue(inputFile.createNewFile());
            }
            assertEquals(MappingPluginState.SUCCESS().getState(), plugins.get("InOutPlugin_1.1.2").mapFile(new File("schema").toPath(), inputFile.toPath(), new File("output").toPath()).getState());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void invalidPath() {
        Map<String, IMappingPlugin> plugins = null;
        try {
            pluginLoader.loadPlugins(new File("./invalid/test"), applicationProperties.getPackagesToScan());
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
        }
    }

//    @Test
//    void invalidPlugin() {
//        Map<String, IMappingPlugin> plugins = null;
//        try {
//            plugins = PluginLoader.loadPlugins(new File("./invalid_plugins"));
//        } catch (Exception e) {
//            fail(e);
//        }
//    }
    @Test
    void nullInput() {
        Map<String, IMappingPlugin> plugins = null;
        try {
            plugins = pluginLoader.loadPlugins(null, null);
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
        }
    }

    @Test
    void emptyinput() {
        Map<String, IMappingPlugin> plugins = null;
        try {
            plugins = pluginLoader.loadPlugins(new File(""), null);
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
        }
    }
}
