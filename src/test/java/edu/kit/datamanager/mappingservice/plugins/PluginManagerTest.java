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

import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ActiveProfiles("test")
class PluginManagerTest {

    @Autowired
    private PluginManager pluginManager;
  
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
        System.out.println(pluginManager.getListOfAvailableValidators());
        assertEquals(2, pluginManager.getListOfAvailableValidators().size());
    }

    @Test
    void mapFileInvalidParameters() {
        try {
            pluginManager.mapFile(null, null, null, null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Plugin ID is null.", e.getMessage());
        }

        try {
            pluginManager.mapFile("test", null, null, null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Path to mapping schema is null.", e.getMessage());
        }

        try {
            pluginManager.mapFile("test", new File("test").toPath(), null, null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Path to input file is null.", e.getMessage());
        }

        try {
            pluginManager.mapFile("test", new File("test").toPath(), new File("testInput").toPath(), null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Path to output file is null.", e.getMessage());
        }

        try {
            pluginManager.mapFile("test", new File("test").toPath(), new File("testInput").toPath(), new File("testOutput").toPath());
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.NOT_FOUND, e.getState());
            assertEquals("Plugin 'test' not found!", e.getMessage());
        }
    }

    @Test
    void mapFile() {
        try {
            File outputFile = new File("/tmp/testOutput");
            pluginManager.mapFile("TEST_0.0.0", new File("mapping-schema").toPath(), new File("input").toPath(), outputFile.toPath());
            assertTrue(outputFile.exists());
            outputFile.delete();
        } catch (MappingPluginException e) {
            e.printStackTrace();
            fail("Mapping failed");
        }
    }
}
