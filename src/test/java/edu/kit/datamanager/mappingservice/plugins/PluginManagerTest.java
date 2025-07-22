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
import edu.kit.datamanager.mappingservice.exception.MappingServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PluginManagerTest {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ApplicationProperties applicationProperties;

    private final String INOUTPLUGIN_ID = "InOutPlugin_2.0.0";

    @BeforeEach
    void setup() throws Exception {
        /*try {
            FileUtils.copyDirectory(Path.of("./plugins").toFile(), Path.of(applicationProperties.getPluginLocation().toURI()).toFile());
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
        pluginManager.reloadPlugins();
    }

    @Test
    @Disabled("Test must be revised as soon as plugin location is configurable")
    void reloadPlugins() {
        assertEquals(2, pluginManager.getPlugins().size());

        pluginManager.unload();

        assertEquals(0, pluginManager.getPlugins().size());

        pluginManager.reloadPlugins();
        assertEquals(2, pluginManager.getPlugins().size());
    }

    @Test
    @Disabled("Test must be revised as soon as plugin location is configurable")
    void getListOfAvailableValidators() {
        System.out.println(pluginManager.listPluginIds());
        assertEquals(2, pluginManager.listPluginIds().size());
    }

    @Test
    void mapFileInvalidParameters() {
        try {
            pluginManager.mapFile(null, null, null, null);
        } catch (MappingServiceException e) {
            assertEquals("PluginId is null.", e.getMessage());
        } catch (MappingPluginException ex) {
            fail("Expected MappingServiceException");
        }

        try {
            pluginManager.mapFile("test", null, null, null);
        } catch (MappingServiceException e) {
            assertEquals("Path to mapping file is null.", e.getMessage());
        } catch (MappingPluginException ex) {
            fail("Expected MappingServiceException");
        }

        try {
            pluginManager.mapFile("test", new File("test").toPath(), null, null);
        } catch (MappingServiceException e) {
            assertEquals("Path to input file is null.", e.getMessage());
        } catch (MappingPluginException ex) {
            fail("Expected MappingServiceException");
        }

        try {
            pluginManager.mapFile("test", new File("test").toPath(), new File("testInput").toPath(), null);
        } catch (MappingServiceException e) {
            assertEquals("Path to output file is null.", e.getMessage());
        } catch (MappingPluginException ex) {
            fail("Expected MappingServiceException");
        }

        try {
            pluginManager.mapFile("test", new File("test").toPath(), new File("testInput").toPath(), new File("testOutput").toPath());
        } catch (MappingServiceException e) {
            fail("Expected MappingPluginException");
        } catch (MappingPluginException ex) {
            assertEquals("Plugin 'test' not found!", ex.getMessage());
        }
    }

    @Test
    void mapFile() {
        try {
            File outputFile = new File("/tmp/testOutput");
            File inputFile = new File("/tmp/testInput");
            if (!inputFile.exists()) {
                assertTrue(inputFile.createNewFile());
            }
            pluginManager.mapFile(INOUTPLUGIN_ID, new File("mapping-schema").toPath(), inputFile.toPath(), outputFile.toPath());
            assertTrue(outputFile.exists());
            assertTrue(inputFile.delete());
            assertTrue(outputFile.delete());
        } catch (MappingPluginException | IOException e) {
            fail("Mapping failed", e);
        }
    }
}
