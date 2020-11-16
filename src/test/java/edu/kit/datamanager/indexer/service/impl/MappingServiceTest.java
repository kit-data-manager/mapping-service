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
package edu.kit.datamanager.indexer.service.impl;

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {"metastore.indexer.mappingsLocation=/tmp/metastore2/mapping"})
public class MappingServiceTest {

  @Autowired
  ApplicationProperties applicationProperties;

  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";

  public MappingServiceTest() {
  }

  @BeforeClass
  public static void setUpClass() {
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

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testConstructor() {
    assertNotNull(new MappingService(applicationProperties));
  }

  @Test
  public void testConstructorRelativePath() throws IOException {
    try {
      ApplicationProperties ap = new ApplicationProperties();
      ap.setMappingsLocation("tmp/relativePath");
      MappingService ms = new MappingService(ap);
      Path saveMapping = ms.saveMapping("x", "x.txt");
      File toFile = saveMapping.getParent().toFile();
      FileUtils.deleteDirectory(toFile);
      assertTrue(true);
    } catch (IndexerException ie) {
      assertTrue(false);
    }
  }

  @Test
  public void testConstructorFailing() throws IOException {
    try {
      new MappingService(null);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    try {
      ApplicationProperties ap = new ApplicationProperties();
      ap.setMappingsLocation("/forbidden");
      new MappingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testSaveMapping() throws IOException {
    System.out.println("saveMapping");
    String content = "content";
    String filename = "firstTest.txt";
    MappingService instance = new MappingService(applicationProperties);
    Path expResult = Paths.get(applicationProperties.getMappingsLocation(), filename);
    Path result = instance.saveMapping(content, filename);
    assertEquals(expResult, result);
    assertTrue(result.toFile().exists());
    assertEquals(content, FileUtils.readFileToString(result.toFile(), StandardCharsets.UTF_8));

  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testSaveMappingWithoutContent() throws IOException {
    System.out.println("saveMapping");
    String content = null;
    String filename = "firstTest.txt";
    MappingService instance = new MappingService(applicationProperties);
    Path expResult = Paths.get(applicationProperties.getMappingsLocation(), filename);
    Path result = instance.saveMapping(content, filename);
    assertEquals(expResult, result);
    assertTrue(result.toFile().exists());
    assertEquals("", FileUtils.readFileToString(result.toFile(), StandardCharsets.UTF_8));

  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testSaveMappingWithInvalidFilename() throws IOException {
    System.out.println("saveMapping");
    String content = "anyContent";
    String filename = "securityException";
    ApplicationProperties ap = new ApplicationProperties();
    ap.setMappingsLocation("/usr/bin");
    MappingService instance = new MappingService(ap);
    Path expResult = Paths.get(applicationProperties.getMappingsLocation(), filename);
    try {
      Path result = instance.saveMapping(content, filename);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
      assertTrue(ie.getMessage().contains("Error writing mapping file"));
    }
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingWithInvalidFilename() throws IOException {
    System.out.println("saveMapping");
    String content = "anyContent";
    String filename = "securityException";
    Path saveMapping = null;
    ApplicationProperties ap = new ApplicationProperties();
    ap.setMappingsLocation("/tmp");
    MappingService instance = new MappingService(ap);
    Path expResult = Paths.get(applicationProperties.getMappingsLocation(), filename);
    try {
      saveMapping = instance.saveMapping(content, filename);
      saveMapping.toFile().setReadOnly();
      Path result = instance.updateMapping(content, filename);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
      assertTrue(ie.getMessage().contains("Error writing mapping file"));
      saveMapping.toFile().setWritable(true);
      saveMapping.toFile().delete();
    }
  }

  /**
   * Test of updateMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMapping() throws IOException {
    System.out.println("updateMapping");
    String content = "content";
    String newContent = "new content";
    String filename = "updateTest.txt";
    MappingService instance = new MappingService(applicationProperties);
    Path expResult = Paths.get(applicationProperties.getMappingsLocation(), filename);
    instance.saveMapping(content, filename);
    Path result = instance.updateMapping(newContent, filename);
    assertEquals(expResult, result);
    assertTrue(result.toFile().exists());
    assertEquals(newContent, FileUtils.readFileToString(result.toFile(), StandardCharsets.UTF_8));
  }

  /**
   * Test of updateMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingWithWrongFile() {
    System.out.println("updateMapping");
    String content = "new content";
    String filename = "invalidFilename.txt";
    MappingService instance = new MappingService(applicationProperties);
    Path expResult = Paths.get(applicationProperties.getMappingsLocation(), filename);
    try {
      Path result = instance.updateMapping(content, filename);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
      assertTrue(ie.getMessage().contains("missing mapping file"));
    }
  }

}
