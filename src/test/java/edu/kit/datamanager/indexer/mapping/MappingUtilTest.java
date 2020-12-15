/*
 * Copyright 2020 Karlsruhe Institute of technologie.
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
package edu.kit.datamanager.indexer.mapping;

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.util.IndexerUtil;
import edu.kit.datamanager.python.util.PythonUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for MappingUtil
 */
public class MappingUtilTest {

  private static URL PYTHON_EXECUTABLE;
  private static URL GEMMA_CLASS;

  private final static Path MAPPING_FILE = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsoluteFile().toPath();
  private final static Path SRC_FILE = new File("src/test/resources/examples/gemma/simple.json").getAbsoluteFile().toPath();
  private final static Path RESULT_FILE = new File("/tmp/result.elastic.json").getAbsoluteFile().toPath();
  private static final String RESULT = "{\n"
          + "  \"Publisher\": \"The publisher\",\n"
          + "  \"Publication Date\": \"2019\"\n"
          + "}";

  public MappingUtilTest() {
  }

  @BeforeClass
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

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    IMappingTool.toolMapper.clear();
  }

  @After
  public void tearDown() {
    if (RESULT_FILE.toFile().exists()) {
      FileUtils.deleteQuietly(RESULT_FILE.toFile());
    }
  }

  /**
   * Test of getMappingTool method, of class IMappingTool.
   */
  @Test
  public void testGetMappingToolWithWrongParameters() {
    System.out.println("testGetMappingToolWithWrongParameters");
    int result;
    ApplicationProperties applicationProperties = new ApplicationProperties();
    MappingUtil instance = new MappingUtil(applicationProperties);
    String[] mapping = {"", "Gemma", null};
    for (String map : mapping) {
      try {
        result = instance.mapFile(MAPPING_FILE, SRC_FILE, RESULT_FILE, map);
        assertTrue("Expected an exception! (mapping = '" + map + "')", false);
      } catch (IndexerException iex) {
        assertTrue(iex.getMessage().contains("is not a valid"));
      }
    }
  }

  /**
   * Test of mapFile method, of class IMappingTool.
   */
  @Test
  public void testMapFile() throws IOException {
    System.out.println("mapFile");
    ApplicationProperties conf = new ApplicationProperties();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    MappingUtil instance = new MappingUtil(conf);
    int expResult = 0;
    int result = instance.mapFile(MAPPING_FILE, SRC_FILE, RESULT_FILE, Mapping.GEMMA.name());
    assertEquals(expResult, result);

    assertTrue(RESULT_FILE.toFile().exists());
    String readFileToString = FileUtils.readFileToString(RESULT_FILE.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
  }

  /**
   * Test of mapFile method, of class IMappingTool.
   */
  @Test
  public void testOverwritingResultFileWithNoContent() throws IOException {
    System.out.println("testOverwritingResultFile");
    ApplicationProperties conf = new ApplicationProperties();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    MappingUtil instance = new MappingUtil(conf);
    FileUtils.writeStringToFile(RESULT_FILE.toFile(), "", StandardCharsets.UTF_8);
    int expResult = 0;
    int result = instance.mapFile(MAPPING_FILE, SRC_FILE, RESULT_FILE, Mapping.GEMMA.name());
    assertEquals(expResult, result);

    assertTrue(RESULT_FILE.toFile().exists());
    String readFileToString = FileUtils.readFileToString(RESULT_FILE.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
  }

  /**
   * Test of mapFile method, of class IMappingTool.
   */
  @Test
  public void testOverwritingResultFileWithReadOnly() throws IOException {
    System.out.println("testOverwritingResultFileWithContent");
    ApplicationProperties conf = new ApplicationProperties();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    MappingUtil instance = new MappingUtil(conf);
    FileUtils.writeStringToFile(RESULT_FILE.toFile(), "", StandardCharsets.UTF_8);
    assertTrue("Set file readonly", RESULT_FILE.toFile().setReadOnly());
    try {
      int result = instance.mapFile(MAPPING_FILE, SRC_FILE, RESULT_FILE, Mapping.GEMMA.name());
      assertTrue("Expected an exception! (overwriting existing file)", false);
    } catch (IndexerException iex) {
      assertTrue(iex.getMessage().contains("Overwriting file"));
    }
    RESULT_FILE.toFile().setWritable(true);
  }

  /**
   * Test of mapFile method, of class IMappingTool.
   */
  @Test
  public void testOverwritingResultFileWithContent() throws IOException {
    System.out.println("testOverwritingResultFileWithContent");
    ApplicationProperties conf = new ApplicationProperties();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    MappingUtil instance = new MappingUtil(conf);
    FileUtils.writeStringToFile(RESULT_FILE.toFile(), "any content", StandardCharsets.UTF_8);
    try {
      int result = instance.mapFile(MAPPING_FILE, SRC_FILE, RESULT_FILE, Mapping.GEMMA.name());
      assertTrue("Expected an exception! (overwriting existing file)", false);
    } catch (IndexerException iex) {
      assertTrue(iex.getMessage().contains("Overwriting file"));
    }
  }

  /**
   * Test of mapFile method, of class MappingUtil.
   */
  @Test
  public void testMapFile_3args() throws IOException {
    System.out.println("mapFile");
    ApplicationProperties conf = new ApplicationProperties();
    // try to map with invalid configuration
    conf.setGemmaLocation(new URL("file:///tmp/invalid_class.py"));
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    MappingUtil instance = new MappingUtil(conf);
    Optional<Path> result;
    result = instance.mapFile(MAPPING_FILE, SRC_FILE, Mapping.GEMMA.name());
    assertFalse(result.isPresent());
    Path invalidFile = Paths.get("invalid", "src", "file");
    result = instance.mapFile(invalidFile, invalidFile, Mapping.GEMMA.name());
    assertFalse(result.isPresent());
    
    // try to map with valid configuration.
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    IMappingTool.toolMapper.clear();
    instance = new MappingUtil(conf);
    result = instance.mapFile(MAPPING_FILE, SRC_FILE, Mapping.GEMMA.name());
    assertTrue(result.isPresent());

    assertTrue(result.get().toFile().exists());
    String readFileToString = FileUtils.readFileToString(result.get().toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
    IndexerUtil.removeFile(result.get());
  }

}
