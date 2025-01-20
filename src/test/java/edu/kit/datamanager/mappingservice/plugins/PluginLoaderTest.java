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
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.io.FileUtils;

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
            plugins = PluginLoader.loadPlugins(Path.of(applicationProperties.getPluginLocation().toURI()).toFile());
        } catch (Exception e) {
            fail(e);
        }
        for (var entry : plugins.entrySet()) {
            System.out.println(entry.getValue().id());
        }
        try {
            assertEquals("TEST_0.0.0", plugins.get("TEST_0.0.0").id());
            assertEquals("TEST", plugins.get("TEST_0.0.0").name());
            assertEquals("Hello world! This is a non functional test plugin.", plugins.get("TEST_0.0.0").description());
            assertEquals("0.0.0", plugins.get("TEST_0.0.0").version());
            assertEquals("https://github.com/kit-data-manager/gemma", plugins.get("TEST_0.0.0").uri());
            assertEquals(MimeTypeUtils.APPLICATION_JSON, plugins.get("TEST_0.0.0").inputTypes()[0]);
            assertEquals(MimeTypeUtils.APPLICATION_JSON, plugins.get("TEST_0.0.0").outputTypes()[0]);
            plugins.get("TEST_0.0.0").setup();
            assertEquals(MappingPluginState.SUCCESS, plugins.get("TEST_0.0.0").mapFile(new File("schema").toPath(), new File("input").toPath(), new File("output").toPath()));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void invalidPath() {
        Map<String, IMappingPlugin> plugins = null;
        try {
            PluginLoader.loadPlugins(new File("./invalid/test"));
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
            plugins = PluginLoader.loadPlugins(null);
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
        }
    }

    @Test
    void emptyinput() {
        Map<String, IMappingPlugin> plugins = null;
        try {
            plugins = PluginLoader.loadPlugins(new File(""));
        } catch (IOException e) {
            fail(e);
        } catch (MappingPluginException validationWarning) {
        }
    }
}
