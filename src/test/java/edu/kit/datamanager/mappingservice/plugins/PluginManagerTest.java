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
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class PluginManagerTest {

    @Test
    void soleInstance() {
        assertNotNull(PluginManager.soleInstance());
        assertEquals(PluginManager.soleInstance(), PluginManager.soleInstance());
    }

    @Test
    void reloadPlugins() {
        assertEquals(2, PluginManager.soleInstance().getPlugins().size());

        try {
            Files.move(new File("./plugins").toPath(), new File("./plugins2").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fail("Could not move plugins folder");
        }

        PluginManager.soleInstance().reloadPlugins();
        assertEquals(0, PluginManager.soleInstance().getPlugins().size());

        try {
            Files.move(new File("./plugins2").toPath(), new File("./plugins").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fail("Could not move plugins2 folder");
        }

        PluginManager.soleInstance().reloadPlugins();
        assertEquals(2, PluginManager.soleInstance().getPlugins().size());
    }

    @Test
    void getListOfAvailableValidators() {
        System.out.println(PluginManager.soleInstance().getListOfAvailableValidators());
        assertEquals(2, PluginManager.soleInstance().getListOfAvailableValidators().size());
    }

    @Test
    void mapFileInvalidParameters() {
        try {
            PluginManager.soleInstance().mapFile(null, null, null, null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Plugin ID is null.", e.getMessage());
        }

        try {
            PluginManager.soleInstance().mapFile("test", null, null, null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Path to mapping schema is null.", e.getMessage());
        }

        try {
            PluginManager.soleInstance().mapFile("test", new File("test").toPath(), null, null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Path to input file is null.", e.getMessage());
        }

        try {
            PluginManager.soleInstance().mapFile("test", new File("test").toPath(), new File("testInput").toPath(), null);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.INVALID_INPUT, e.getState());
            assertEquals("Path to output file is null.", e.getMessage());
        }

        try {
            PluginManager.soleInstance().mapFile("test", new File("test").toPath(), new File("testInput").toPath(), new File("testOutput").toPath());
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.NOT_FOUND, e.getState());
            assertEquals("Plugin 'test' not found!", e.getMessage());
        }
    }

    @Test
    void mapFile() {
        try {
            File outputFile = new File("src/test/resources/testOutput");
            outputFile.delete();
            assertFalse(outputFile.exists());
            PluginManager.soleInstance().mapFile("TEST_0.0.0", new File("mapping-schema").toPath(), new File("input").toPath(), outputFile.toPath());
            assertTrue(outputFile.exists());
            outputFile.delete();
        } catch (MappingPluginException e) {
            fail("Mapping failed");
        }
    }
}