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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
class PluginLoaderTest {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private PluginLoader pluginLoader;

    @Autowired
    private ApplicationProperties applicationProperties;

    private final String INOUTPLUGIN_VERSION = "2.0.0";
    private final String INOUTPLUGIN_NAME = "InOutPlugin";
    private final String INOUTPLUGIN_ID = INOUTPLUGIN_NAME + "_" + INOUTPLUGIN_VERSION;


    @BeforeEach
    void setUp() throws Exception {
        //not needed as plugins are part of the service now
        /*try {
            FileUtils.copyDirectory(Path.of("./plugins").toFile(), Path.of(applicationProperties.getPluginLocation().toURI()).toFile());
        } catch (IOException ex) {
            fail("IOException during setup occurred.", ex);
        }*/
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
            assertEquals(INOUTPLUGIN_ID, plugins.get(INOUTPLUGIN_ID).id());
            assertEquals(INOUTPLUGIN_NAME, plugins.get(INOUTPLUGIN_ID).name());
            assertEquals("Simple plugin for testing just returning the input file.", plugins.get(INOUTPLUGIN_ID).description());
            assertEquals(INOUTPLUGIN_VERSION, plugins.get(INOUTPLUGIN_ID).version());
            assertEquals("https://github.com/kit-data-manager/mapping-service", plugins.get(INOUTPLUGIN_ID).uri());
            assertEquals("application/*", plugins.get(INOUTPLUGIN_ID).inputTypes()[0]);
            assertEquals("application/*", plugins.get(INOUTPLUGIN_ID).outputTypes()[0]);
            plugins.get(INOUTPLUGIN_ID).setup(applicationProperties);
            File inputFile = new File("/tmp/inputFile");
            if (!inputFile.exists()) {
                Assertions.assertTrue(inputFile.createNewFile());
            }
            assertEquals(MappingPluginState.SUCCESS().getState(), plugins.get(INOUTPLUGIN_ID).mapFile(new File("schema").toPath(), inputFile.toPath(), new File("output").toPath()).getState());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void invalidPath() {
        try {
            pluginLoader.loadPlugins(new File("./invalid/test"), applicationProperties.getPackagesToScan());
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
            fail("MappingPluginException caught when loading plugins from invalid path.", validationWarning);
        }
    }

    @Test
    void nullInput() {
        try {
            pluginLoader.loadPlugins(null, null);
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
            fail("MappingPluginException caught while loading plugins from invalid path.", validationWarning);
        }
    }

    @Test
    void testEmptyInput() {
        try {
            pluginLoader.loadPlugins(new File(""), null);
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
            fail("MappingPluginException caught when loading plugins from invalid path.", validationWarning);
        }
    }
}
