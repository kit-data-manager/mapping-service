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
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.mapping.Mapping;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.aspectj.util.FileUtil;
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
@TestPropertySource(properties = {"metastore.indexer.mappingsLocation=/tmp/metastore2/test/mappingservice"})
public class MappingServiceTest {

  @Autowired
  ApplicationProperties applicationProperties;

  @Autowired
  MappingService mappingService;

  @Autowired
  IMappingRecordDao mappingRepo;

  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/test/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mappingservice/";
  private static final String MAPPING_ID = "my_mapping";
  private static final File JSON_MAPPING_FILE = new File("src/test/resources/mapping/gemma/simple.mapping");
  private static final File XML_MAPPING_FILE = new File("src/test/resources/mapping/gemma/simple.xml.mapping");
  private static final File JSON_SRC_FILE = new File("src/test/resources/examples/gemma/simple.json");
  private static final File XML_SRC_FILE = new File("src/test/resources/examples/gemma/simple.xml");
  private static final File RESULT_FILEDe = new File("src/test/resources/result/gemma/simple.elastic.json");

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
    mappingRepo.deleteAll();
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
    String mappingsDir = "testMapping";
    ApplicationProperties applicationProperties = new ApplicationProperties();
    applicationProperties.setMappingsLocation("testMapping");
    File file = new File(mappingsDir);
    assertFalse(file.exists());
    MappingService mappingService = new MappingService(applicationProperties);
    assertTrue(file.exists());
    FileUtils.deleteDirectory(file);
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
  public void testCreateMapping() throws IOException {
    System.out.println("testCreateMapping");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    mappingService.createMapping(mappingContent, mappingRecord);
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    MappingRecord get = mappingRepo.findAll().get(0);
    assertTrue(get.getMappingDocumentUri().contains(MAPPING_ID));
    assertTrue(get.getMappingDocumentUri().contains(Mapping.GEMMA.toString()));
    assertTrue(get.getMappingDocumentUri().contains(TEMP_DIR_4_MAPPING));
    File mappingFile = new File(get.getMappingDocumentUri());
    assertTrue(mappingFile.exists());
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testCreateMappingWithoutContent() throws IOException {
    System.out.println("testCreateMappingWithoutContent");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappinDigContent = null;
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.createMapping(mappinDigContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testSaveMappingWithNoMapping() throws IOException {
    System.out.println("testSaveMappingWithNoMapping");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(null);
    String mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.createMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testSaveMappingWithUnknownMapping() throws IOException {
    System.out.println("testSaveMappingWithUnknownMapping");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType("unknownMappingType");
    String mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.createMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testCreateMappingTwice() throws IOException {
    System.out.println("testCreateMapping");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    mappingService.createMapping(mappingContent, mappingRecord);
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.createMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMapping() throws IOException {
    System.out.println("testUpdateMapping");
    testCreateMapping();
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappingContent = FileUtil.readAsString(XML_MAPPING_FILE);
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    mappingService.updateMapping(mappingContent, mappingRecord);
    assertEquals(1, mappingRepo.count());
    assertEquals(2, new File(TEMP_DIR_4_MAPPING).list().length);
    MappingRecord first = mappingRepo.findAll().get(0);
    assertTrue(first.getMappingDocumentUri().contains(MAPPING_ID));
    assertTrue(first.getMappingDocumentUri().contains(Mapping.GEMMA.toString()));
    assertTrue(first.getMappingDocumentUri().contains(TEMP_DIR_4_MAPPING));
    File mappingFile = new File(first.getMappingDocumentUri());
    assertTrue(mappingFile.exists());
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingWithoutCreate() throws IOException {
    System.out.println("testUpdateMappingWithoutCreate");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappingContent = FileUtil.readAsString(XML_MAPPING_FILE);
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.updateMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingWithoutContent() throws IOException {
    System.out.println("testUpdateMappingWithoutContent");
    testCreateMapping();
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappingContent = null;
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.updateMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    MappingRecord first = mappingRepo.findAll().get(0);
    File mappingFile = new File(first.getMappingDocumentUri());
    assertTrue(mappingFile.exists());
    mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingWithNoMapping() throws IOException {
    System.out.println("testUpdateMappingWithNoMapping");
    testCreateMapping();
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(null);
    String mappingContent = FileUtil.readAsString(XML_MAPPING_FILE);
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.createMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    MappingRecord first = mappingRepo.findAll().get(0);
    File mappingFile = new File(first.getMappingDocumentUri());
    assertTrue(mappingFile.exists());
    mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingWithUnknownMapping() throws IOException {
    System.out.println("testUpdateMappingWithUnknownMapping");
    testCreateMapping();
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType("unknownMappingType");
    String mappingContent = FileUtil.readAsString(XML_MAPPING_FILE);
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.createMapping(mappingContent, mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    MappingRecord first = mappingRepo.findAll().get(0);
    File mappingFile = new File(first.getMappingDocumentUri());
    assertTrue(mappingFile.exists());
    mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));
  }

  /**
   * Test of saveMapping method, of class MappingService.
   */
  @Test
  public void testUpdateMappingTwice() throws IOException, InterruptedException {
    System.out.println("testUpdateMappingTwice");
    testUpdateMapping();
    Thread.sleep(1000);
    String newMappingContent = "any content";
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    mappingRecord.setMappingType(Mapping.GEMMA.toString());
    String mappingContent = newMappingContent;
    assertEquals(1, mappingRepo.count());
    mappingService.updateMapping(mappingContent, mappingRecord);
    assertEquals(1, mappingRepo.count());
    assertEquals(3, new File(TEMP_DIR_4_MAPPING).list().length);
    MappingRecord first = mappingRepo.findAll().get(0);
    assertTrue(first.getMappingDocumentUri().contains(MAPPING_ID));
    assertTrue(first.getMappingDocumentUri().contains(Mapping.GEMMA.toString()));
    assertTrue(first.getMappingDocumentUri().contains(TEMP_DIR_4_MAPPING));
    File mappingFile = new File(first.getMappingDocumentUri());
    assertTrue(mappingFile.exists());
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));
  }

  /**
   * Test of deleteMapping method, of class MappingService.
   */
  @Test
  public void testDeleteMapping() throws Exception {
    System.out.println("deleteMapping");
    testCreateMapping();
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    String mappingContent = FileUtil.readAsString(JSON_MAPPING_FILE);
    assertEquals(1, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    mappingService.deleteMapping(mappingRecord);
    assertEquals(0, mappingRepo.count());
    assertEquals(1, new File(TEMP_DIR_4_MAPPING).list().length);
    File mappingFile = new File(TEMP_DIR_4_MAPPING).listFiles()[0];
    assertTrue(mappingFile.exists());
    assertEquals(mappingContent, FileUtil.readAsString(mappingFile));

  }

  /**
   * Test of deleteMapping method, of class MappingService.
   */
  @Test
  public void testDeleteNotExistingMapping() throws Exception {
    System.out.println("testDeleteNotExistingMapping");
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setId(MAPPING_ID);
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);
    try {
      mappingService.deleteMapping(mappingRecord);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    assertEquals(0, mappingRepo.count());
    assertEquals(0, new File(TEMP_DIR_4_MAPPING).list().length);

  }

  /**
   * Test of executeMapping method, of class MappingService.
   */
  @Test
  public void testExecuteMapping() {
    System.out.println("executeMapping");
//    URI contentUrl = null;
//    String mappingId = "";
//    MappingService instance = null;
//    Optional<Path> expResult = null;
//    Optional<Path> result = instance.executeMapping(contentUrl, mappingId);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
  }

}
