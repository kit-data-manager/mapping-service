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
package edu.kit.datamanager.mappingservice.rest;

import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.MimeTypeUtils;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PluginInformationTest {

    private PluginInformation pluginInformation;

    @Autowired
    private PluginManager pluginManager;

    @BeforeEach
    void setUp() {
        pluginInformation = new PluginInformation();
        pluginInformation.id = "TEST_0.0.0";
        pluginInformation.name = "TEST";
        pluginInformation.version = "0.0.0";
        pluginInformation.description = "Hello world! This is a non functional test plugin.";
        pluginInformation.uri = "https://github.com/kit-data-manager/gemma";
        pluginInformation.inputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString(), MimeTypeUtils.APPLICATION_XML.toString()};
        pluginInformation.outputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString()};
    }

    @Test
    void testEquals() {
        assertEquals(pluginInformation, pluginInformation);
        assertFalse(pluginInformation.equals(null));
        assertFalse(pluginInformation.equals(new Object()));
        PluginInformation test = new PluginInformation();
        test.id = null;
        assertNotEquals(pluginInformation, test);
        test.id = "invalid";
        assertNotEquals(pluginInformation, test);
        test.id = "TEST_0.0.0";
        assertEquals(pluginInformation, test);
        test.name = "Invalid";
        test.version = "0.0.0";
        test.description = "Hello world! This is a non functional test plugin.";
        test.uri = "https://github.com/kit-data-manager/gemma";
        test.inputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString(), MimeTypeUtils.APPLICATION_XML.toString()};
        test.outputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString()};
        assertEquals(pluginInformation, test);
    }

    @Test
    void testHashCode() {
        assertEquals(pluginInformation.hashCode(), pluginInformation.hashCode());
        PluginInformation test = new PluginInformation();
        test.id = "invalid";
        test.name = "TEST";
        test.version = "0.0.0";
        test.description = "Hello world! This is a non functional test plugin.";
        test.uri = "https://github.com/kit-data-manager/gemma";
        test.inputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString(), MimeTypeUtils.APPLICATION_XML.toString()};
        test.outputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString()};
//        assertNotEquals(pluginInformation.hashCode(), test.hashCode());
        test.id = "TEST_0.0.0";
//        test.name = "invalid";
//        assertNotEquals(pluginInformation.hashCode(), test.hashCode());
        assertEquals(pluginInformation.hashCode(), test.hashCode());
    }

    @Test
    void getId() {
        assertEquals("TEST_0.0.0", pluginInformation.getId());
        PluginInformation test = new PluginInformation();
        test.id = "invalid";
        assertEquals("invalid", test.getId());
    }

    @Test
    void getName() {
        assertEquals("TEST", pluginInformation.getName());
        PluginInformation test = new PluginInformation();
        test.name = "invalid";
        assertEquals("invalid", test.getName());
    }

    @Test
    void getVersion() {
        assertEquals("0.0.0", pluginInformation.getVersion());
        PluginInformation test = new PluginInformation();
        test.version = "invalid";
        assertEquals("invalid", test.getVersion());
    }

    @Test
    void getDescription() {
        assertEquals("Hello world! This is a non functional test plugin.", pluginInformation.getDescription());
        PluginInformation test = new PluginInformation();
        test.description = "invalid";
        assertEquals("invalid", test.getDescription());
    }

    @Test
    void getUri() {
        assertEquals("https://github.com/kit-data-manager/gemma", pluginInformation.getUri());
        PluginInformation test = new PluginInformation();
        test.uri = "invalid";
        assertEquals("invalid", test.getUri());
    }

    @Test
    void getInputTypes() {
        assertArrayEquals(new String[]{MimeTypeUtils.APPLICATION_JSON.toString(), MimeTypeUtils.APPLICATION_XML.toString()}, pluginInformation.getInputTypes());
        PluginInformation test = new PluginInformation();
        test.inputTypes = new String[]{"invalid"};
        assertArrayEquals(new String[]{"invalid"}, test.getInputTypes());
    }

    @Test
    void getOutputTypes() {
        assertArrayEquals(new String[]{MimeTypeUtils.APPLICATION_JSON.toString()}, pluginInformation.getOutputTypes());
        PluginInformation test = new PluginInformation();
        test.outputTypes = new String[]{"invalid"};
        assertArrayEquals(new String[]{"invalid"}, test.getOutputTypes());
    }

    @Test
    void setId() {
        PluginInformation test = new PluginInformation();
        assertNull(test.id);
        test.setId("invalid");
        assertEquals("invalid", test.id);
    }

    @Test
    void setName() {
        PluginInformation test = new PluginInformation();
        assertNull(test.name);
        test.setName("invalid");
        assertEquals("invalid", test.name);
    }

    @Test
    void setVersion() {
        PluginInformation test = new PluginInformation();
        assertNull(test.version);
        test.setVersion("invalid");
        assertEquals("invalid", test.version);
    }

    @Test
    void setDescription() {
        PluginInformation test = new PluginInformation();
        assertNull(test.description);
        test.setDescription("invalid");
        assertEquals("invalid", test.description);
    }

    @Test
    void setUri() {
        PluginInformation test = new PluginInformation();
        assertNull(test.uri);
        test.setUri("invalid");
        assertEquals("invalid", test.uri);
    }

    @Test
    void setInputTypes() {
        PluginInformation test = new PluginInformation();
        assertNull(test.inputTypes);
        test.setInputTypes(new String[]{"invalid"});
        assertArrayEquals(new String[]{"invalid"}, test.inputTypes);
    }

    @Test
    void setOutputTypes() {
        PluginInformation test = new PluginInformation();
        assertNull(test.outputTypes);
        test.setOutputTypes(new String[]{"invalid"});
        assertArrayEquals(new String[]{"invalid"}, test.outputTypes);
    }

    @Test
    void testToString() {
        assertEquals(pluginInformation.toString(), pluginInformation.toString());
        assertNotNull(pluginInformation.toString());
        PluginInformation test = new PluginInformation();
        test.id = "invalid";
        assertNotEquals(pluginInformation.toString(), test.toString());
        test.id = "TEST_0.0.0";
        test.name = "TEST";
        test.version = "0.0.0";
        test.description = "Hello world! This is a non functional test plugin.";
        test.uri = "https://github.com/kit-data-manager/gemma";
        test.inputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString(), MimeTypeUtils.APPLICATION_XML.toString()};
        test.outputTypes = new String[]{MimeTypeUtils.APPLICATION_JSON.toString()};
        assertEquals(pluginInformation.toString(), test.toString());
    }

    @Test
    void testIDConstructor() {
        try {
            assertEquals(pluginInformation, new PluginInformation("TEST_0.0.0", pluginManager));
        } catch (MappingPluginException e) {
            e.printStackTrace();
            fail(e);
        }
        try {
            new PluginInformation(null, pluginManager);
            fail("Expected exception");
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.NOT_FOUND, e.getState());
        }
    }
}
