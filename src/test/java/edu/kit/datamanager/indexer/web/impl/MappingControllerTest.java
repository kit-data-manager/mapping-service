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
package edu.kit.datamanager.indexer.web.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.domain.acl.AclEntry;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.WebRequest;

/**
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {"server.port=41300"})
public class MappingControllerTest {

  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";
  private static final String MAPPING_ID = "my_dc";
  private static final String MAPPING_TYPE = "GEMMA";

  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private FilterChainProxy springSecurityFilterChain;
  @Autowired
  private IMappingRecordDao mappingRecordDao;
  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  public MappingControllerTest() {
  }

  @Before
  public void setUp() {
    mappingRecordDao.deleteAll();
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
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .addFilters(springSecurityFilterChain)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMapping() throws Exception {
    System.out.println("createMapping");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingNoRecord() throws Exception {
    System.out.println("testCreateMappingNoRecord");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingEmptyRecord() throws Exception {
    System.out.println("testCreateMappingEmptyRecord");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", "".getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testCreateMappingNoMapping() throws Exception {
    System.out.println("testCreateMappingNoMapping");
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  @Test
  public void testCreateMappingWrongRecord() throws Exception {
    System.out.println("testCreateMappingEmptyMapping");
    String mappingContent = "";
    MappingRecord record = new MappingRecord();
    record.setMappingId(null);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    record.setMappingId(MAPPING_ID);
    record.setMappingType(null);
    recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingTwice() throws Exception {
    System.out.println("testCreateMappingTwice");
    testCreateMapping();
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isConflict()).andReturn();
  }

  /**
   * Test of createMapping method, of class MappingController.
   */
  @Test
  public void testCreateMappingWithAcl() throws Exception {
    System.out.println("testCreateMappingWithAcl");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    Set<AclEntry> aclEntries = new HashSet<>();
    aclEntries.add(new AclEntry("test2", PERMISSION.ADMINISTRATE));
    aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
  }

  /**
   * Test of getMappingById method, of class MappingController.
   */
  @Test
  public void testGetMappingById() throws UnsupportedEncodingException, JsonProcessingException, Exception {
    System.out.println("getMappingById");
    testCreateMapping();
    String mappingId = MAPPING_ID;
    String mappingType = MAPPING_TYPE;
    String getMappingIdUrl = "/api/v1/mapping/" + mappingId + "/" + mappingType;
    MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    MappingRecord result = map.readValue(res.getResponse().getContentAsString(), MappingRecord.class);
    Assert.assertNotNull(result);
    Assert.assertEquals(mappingId, result.getMappingId());
    Assert.assertEquals(mappingType, result.getMappingType());
    Assert.assertTrue(result.getMappingDocumentUri().contains(getMappingIdUrl));
  }

  /**
   * Test of getMappingById method, of class MappingController.
   */
  @Test
  public void testGetMappingByIdWithInvalidMapping() throws UnsupportedEncodingException, JsonProcessingException, Exception {
    System.out.println("testGetMappingByIdWithInvalidMapping");
    testCreateMapping();
    String mappingId = "invalidMappingId";
    String mappingType = MAPPING_TYPE;
    String getMappingIdUrl = "/api/v1/mapping/" + mappingId + "/" + mappingType;
    MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    mappingId = MAPPING_ID;
    mappingType = "invalidMappingType";
    getMappingIdUrl = "/api/v1/mapping/" + mappingId + "/" + mappingType;
    res = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test of getMappingById method, of class MappingController.
   */
  @Test
  public void testGetMappingDocumentById() throws UnsupportedEncodingException, JsonProcessingException, Exception {
    System.out.println("testGetMappingDocumentById");
    String expResult = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    testCreateMapping();
    String mappingId = MAPPING_ID;
    String mappingType = MAPPING_TYPE;
    String getMappingIdUrl = "/api/v1/mapping/" + mappingId + "/" + mappingType;
    MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isOk()).andReturn();
    String result = res.getResponse().getContentAsString();
    Assert.assertNotNull(result);
    Assert.assertEquals(expResult, result);
  }

  @Test
  public void testGetMappingDocumentByIdWithInvalidMapping() throws UnsupportedEncodingException, JsonProcessingException, Exception {
    System.out.println("testGetMappingDocumentByIdWithInvalidMapping");
    testCreateMapping();
    String mappingId = "invalidMappingId";
    String mappingType = MAPPING_TYPE;
    String getMappingIdUrl = "/api/v1/mapping/" + mappingId + "/" + mappingType;
    MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    mappingId = MAPPING_ID;
    mappingType = "invalidMappingType";
    getMappingIdUrl = "/api/v1/mapping/" + mappingId + "/" + mappingType;
    res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isNotFound()).andReturn();
  }

  /**
   * Test of getMappings method, of class MappingController.
   */
  @Test
  public void testGetMappings() throws UnsupportedEncodingException, JsonProcessingException, Exception {
    System.out.println("getMappings");
    create2Mappings();
    String mappingId = MAPPING_ID;
    String mappingType = MAPPING_TYPE;
    String getMappingIdUrl = "/api/v1/mapping";
    MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    MappingRecord[] result = map.readValue(res.getResponse().getContentAsString(), MappingRecord[].class);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.length);
    for (MappingRecord item : result) {
      Assert.assertEquals(mappingType, item.getMappingType());
      Assert.assertTrue(item.getMappingDocumentUri().contains(mappingType));
    }
  }

  /**
   * Test of getMappings method, of class MappingController.
   */
  @Test
  public void testGetMappingsWithFilter() throws UnsupportedEncodingException, JsonProcessingException, Exception {
    System.out.println("getMappings");
    create2Mappings();
    String mappingId = MAPPING_ID;
    String mappingType = MAPPING_TYPE;
    String getMappingIdUrl = "/api/v1/mapping";
    MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).param("mappingType", MAPPING_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
    ObjectMapper map = new ObjectMapper();
    MappingRecord[] result = map.readValue(res.getResponse().getContentAsString(), MappingRecord[].class);
    Assert.assertNotNull(result);
    Assert.assertEquals(2, result.length);
    for (MappingRecord item : result) {
      Assert.assertEquals(mappingType, item.getMappingType());
      Assert.assertTrue(item.getMappingDocumentUri().contains(mappingType));
    }
    res = this.mockMvc.perform(get(getMappingIdUrl).param("mappingId", MAPPING_ID)).andDo(print()).andExpect(status().isOk()).andReturn();
    map = new ObjectMapper();
    result = map.readValue(res.getResponse().getContentAsString(), MappingRecord[].class);
    Assert.assertNotNull(result);
    Assert.assertEquals(1, result.length);
    for (MappingRecord item : result) {
      Assert.assertEquals(MAPPING_ID, item.getMappingId());
      Assert.assertEquals(mappingType, item.getMappingType());
      Assert.assertTrue(item.getMappingDocumentUri().contains(mappingType));
    }

  }
//
//  /**
//   * Test of updateMapping method, of class MappingController.
//   */
//  @Test
//  public void testUpdateMapping() throws JsonProcessingException {
//    System.out.println("updateMapping");
//    String mappingId = MAPPING_ID;
//    String mappingType = MAPPING_TYPE;
//    MappingRecord record = new MappingRecord();
//    record.setMappingId(mappingId);
//    record.setMappingType(mappingType);
//    ObjectMapper mapper = new ObjectMapper();
//    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
//    MultipartFile document = null;
//    WebRequest request = null;
//    HttpServletResponse response = null;
//    UriComponentsBuilder uriBuilder = null;
//    MappingController instance = new MappingController();
//    ResponseEntity expResult = null;
//    ResponseEntity result = instance.updateMapping(recordFile, document, request, response, uriBuilder);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of deleteMapping method, of class MappingController.
//   */
//  @Test
//  public void testDeleteMapping() {
//    System.out.println("deleteMapping");
//    String mappingId = MAPPING_ID;
//    String mappingType = MAPPING_TYPE;
//    WebRequest wr = null;
//    HttpServletResponse hsr = null;
//    MappingController instance = new MappingController();
//    ResponseEntity expResult = null;
//    ResponseEntity result = instance.deleteMapping(mappingId, mappingType, wr, hsr);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }
//
//  /**
//   * Test of mergeRecords method, of class MappingController.
//   */
//  @Test
//  public void testMergeRecords() {
//    System.out.println("mergeRecords");
//    MappingRecord managed = null;
//    MappingRecord provided = null;
//    MappingController instance = new MappingController();
//    MappingRecord expResult = null;
//    MappingRecord result = instance.mergeRecords(managed, provided);
//    assertEquals(expResult, result);
//    // TODO review the generated test code and remove the default call to fail.
//    fail("The test case is a prototype.");
//  }

  private void create2Mappings() throws Exception {
    System.out.println("createMapping");
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
    MappingRecord record = new MappingRecord();
//    record.setMappingId("my_id");
    record.setMappingId(MAPPING_ID);
    record.setMappingType(MAPPING_TYPE);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();

    mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);
    record.setMappingId("anotherMappingId");

    recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
    mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
  }

}
