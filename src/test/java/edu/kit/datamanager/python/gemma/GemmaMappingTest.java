/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.python.gemma;

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.python.util.PythonUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GemmaMappingTest {

  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/indexer/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";

  private static final String RESULT = "{\n"
          + "  \"Publisher\": \"The publisher\",\n"
          + "  \"Publication Date\": \"2019\"\n"
          + "}";

  private static URL PYTHON_EXECUTABLE;
  private static URL GEMMA_CLASS;

  public GemmaMappingTest() {
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
    PYTHON_EXECUTABLE = new File(pythonExecutable.trim()).toURI().toURL();
    GEMMA_CLASS = new File("src/test/resources/python/mapping_single.py").toURI().toURL();
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    try {
      try (Stream<Path> walk = Files.walk(Paths.get(URI.create("file://" + TEMP_DIR_4_MAPPING)))) {
        walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
      }
      Paths.get(TEMP_DIR_4_MAPPING).toFile().mkdir();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of mapFile method, of class GemmaMapping.
   */
  @Test
  public void testRunGemma() throws IOException, URISyntaxException {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("runGemma");
    Path mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsoluteFile().toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/simple.json").getAbsoluteFile().toPath();
    Path resultFile = new File("/tmp/result.elastic.json").getAbsoluteFile().toPath();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    GemmaMapping instance = new GemmaMapping(conf2ApplicationProperties(conf));
    int expResult = 0;
    int result = instance.mapFile(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);

    assertTrue(resultFile.toFile().exists());
    String readFileToString = FileUtils.readFileToString(resultFile.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
  }

  /**
   * Test of mapFile method, of class GemmaMapping.
   */
  @Test
  public void testRunGemmaXmlMapping() throws IOException, URISyntaxException {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("testRunGemmaXmlMapping");
    Path mappingFile = new File("src/test/resources/mapping/gemma/simple.xml.mapping").getAbsoluteFile().toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/simple.xml").getAbsoluteFile().toPath();
    Path resultFile = new File("/tmp/result.xml.elastic.json").getAbsoluteFile().toPath();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    GemmaMapping instance = new GemmaMapping(conf2ApplicationProperties(conf));
    int expResult = 0;
    int result = instance.mapFile(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);

    assertTrue(resultFile.toFile().exists());
    String readFileToString = FileUtils.readFileToString(resultFile.toFile(), StandardCharsets.UTF_8);
    assertEquals(RESULT, readFileToString);
    FileUtils.deleteQuietly(resultFile.toFile());
    assertFalse("Can't remove result file '"+ resultFile.toString() + "'!", resultFile.toFile().exists());
  }

  /**
   * Test of mapFile method, of class GemmaMapping.
   */
  @Test
  public void testRunGemmaWrongMapping() throws IOException, URISyntaxException {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("testRunGemmaWrongMapping");
    Path mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsoluteFile().toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/notexists").getAbsoluteFile().toPath();
    Path resultFile = new File("/tmp/invalid_result.elastic.json").getAbsoluteFile().toPath();
    conf.setGemmaLocation(GEMMA_CLASS);
    conf.setPythonLocation(PYTHON_EXECUTABLE);
    GemmaMapping instance = new GemmaMapping(conf2ApplicationProperties(conf));
    int expResult = PythonUtils.EXECUTION_ERROR;
    int result = instance.mapFile(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);
    assertFalse(resultFile.toFile().exists());
  }
  
  private ApplicationProperties conf2ApplicationProperties(GemmaConfiguration configuration) {
    ApplicationProperties ap = new ApplicationProperties();
    ap.setGemmaLocation(configuration.getGemmaLocation());
    ap.setPythonLocation(configuration.getPythonLocation());
    return ap;
  }

}
