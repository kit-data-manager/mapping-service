/*
 * Copyright 2020 hartmann-v.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.mapping;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.python.gemma.GemmaMapping;
import edu.kit.datamanager.mappingservice.python.util.PythonUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hartmann-v
 */
public class IMappingToolTest {

    private static URL PYTHON_EXECUTABLE;
    private static URL GEMMA_CLASS;

    private static final String RESULT = "{\n"
            + "  \"Publisher\": \"The publisher\",\n"
            + "  \"Publication Date\": \"2019\"\n"
            + "}";

    public IMappingToolTest() {
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        // Determine python location
        OutputStream os = new ByteArrayOutputStream();
        PythonUtils.run("which", "python3", os, null);
        String pythonExecutable = os.toString();
        os.flush();
        if (pythonExecutable.trim().isEmpty()) {
            PythonUtils.run("which", "python", os, null);
            pythonExecutable = os.toString();
        }
        if (pythonExecutable.trim().isEmpty()) {
            throw new IOException("Python seems not to be available!");
        }
        System.out.println("Location of python: " + pythonExecutable);
        PYTHON_EXECUTABLE = new File(pythonExecutable.trim()).getAbsoluteFile().toURI().toURL();
        GEMMA_CLASS = new URL("file:src/test/resources/python/mapping_single.py");
    }

    /**
     * Test of getMappingTool method, of class IMappingTool.
     */
    @Test
    public void testGetMappingToolWithWrongParameters() {
        System.out.println("testGetMappingToolWithWrongParameters");
        ApplicationProperties applicationProperties = null;
        String[] mapping = {"", "Gemma", null};
        for (String map : mapping) {
            try {
                IMappingTool result = IMappingTool.getMappingTool(applicationProperties, map);
                fail("Expected an exception! (mapping = '" + map + "')");
            } catch (MappingException iex) {
                assertTrue(true);
            }
        }
    }

    /**
     * Test of getMappingTool method, of class IMappingTool.
     */
    @Test
    public void testGetMappingTool() {
        System.out.println("getMappingTool");
        ApplicationProperties applicationProperties = new ApplicationProperties();
        String mapping = "GEMMA";
        IMappingTool result = IMappingTool.getMappingTool(applicationProperties, mapping);
        assertTrue(result instanceof GemmaMapping);
    }

    /**
     * Test of mapFile method, of class IMappingTool.
     */
    @Test
    public void testMapFile() throws IOException {
        System.out.println("mapFile");
        ApplicationProperties conf = new ApplicationProperties();
        Path mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsoluteFile().toPath();
        Path srcFile = new File("src/test/resources/examples/gemma/simple.json").getAbsoluteFile().toPath();
        Path resultFile = new File("/tmp/result.elastic.json").getAbsoluteFile().toPath();
        conf.setGemmaLocation(GEMMA_CLASS);
        conf.setPythonLocation(PYTHON_EXECUTABLE);
        IMappingTool instance = IMappingTool.getMappingTool(conf, Mapping.GEMMA.name());
        int expResult = 0;
        int result = instance.mapFile(mappingFile, srcFile, resultFile);
        assertEquals(expResult, result);

        assertTrue(resultFile.toFile().exists());
        String readFileToString = FileUtils.readFileToString(resultFile.toFile(), StandardCharsets.UTF_8);
        assertEquals(RESULT, readFileToString);
    }

}
