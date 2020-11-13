/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.python.gemma;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GemmaUtilTest {
  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/indexer/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";

  
  public GemmaUtilTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
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
   * Test of runGemma method, of class GemmaUtil.
   */
  @Test
  public void testRunGemma() {
    GemmaConfiguration conf = new GemmaConfiguration();
    System.out.println("runGemma");
    Path mappingFile = new File("src/test/resources/mapping/gemma/python/single.mapping").toPath();
    Path srcFile = new File("src/test/resources/examples/gemma/simple.json").toPath();
    Path resultFile = new File("/tmp/result.elastic.json").toPath();
    conf.setGemmaLocation(srcFile.toString());
    conf.setPythonLocation("/usr/bin/python3");
    GemmaUtil instance = new GemmaUtil(conf);
    int expResult = 0;
    int result = instance.runGemma(mappingFile, srcFile, resultFile);
    assertEquals(expResult, result);
    
    assertTrue(resultFile.toFile().exists());
  }
  
}
