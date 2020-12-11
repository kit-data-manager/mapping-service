/*
 * Copyright 2018 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.indexer.documentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.indexer.dao.IAclEntryDao;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.domain.acl.AclEntry;
import static edu.kit.datamanager.indexer.mapping.Mapping.GEMMA;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 */
//@ActiveProfiles("doc")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {"server.port=41500"})
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:db_doc;DB_CLOSE_DELAY=-1"})
@TestPropertySource(properties = {"metastore.indexer.mappingsLocation=file:///tmp/metastore2/restdocu/mapping"})
public class MappingControllerDocumentationTest {

  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;

  @Autowired
  private IMappingRecordDao mappingRecordDao;
  @Autowired
  private IAclEntryDao aclEntryDao;
  @Autowired
  private FilterChainProxy springSecurityFilterChain;

  @Rule
  public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

  private final static String EXAMPLE_SCHEMA_ID_XML = "my_first_xsd";
  private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/restdocu/";
  private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";
  private final static String EXAMPLE_SCHEMA_ID_JSON = "my_first_json";

  @Before
  public void setUp() throws JsonProcessingException {
    mappingRecordDao.deleteAll();
    aclEntryDao.deleteAll();
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

  @Test
  public void documentMappingRegistry() throws Exception {
    MappingRecord record = new MappingRecord();
    // register a first mapping for xml
    // Create a mapping record 
    record.setMappingId(EXAMPLE_SCHEMA_ID_XML);
    record.setMappingType(GEMMA.name());

    File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
    String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.xml.mapping"), StandardCharsets.UTF_8);
    Set<AclEntry> aclEntries = new HashSet<>();
//    aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
//    aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
//    record.setAcl(aclEntries);
    ObjectMapper mapper = new ObjectMapper();

    MockMultipartFile recordFile = new MockMultipartFile("record", "record_xml.json", "application/json", mapper.writeValueAsString(record).getBytes());
    MockMultipartFile mappingFile = new MockMultipartFile("document", EXAMPLE_SCHEMA_ID_XML + "4gemma.mapping", "application/json", mappingContent.getBytes());

    Assert.assertEquals(0, mappingsDir.list().length);
    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andDo(document("post-xml-mapping", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
    Assert.assertEquals(1, mappingsDir.list().length);
    // register a second mapping for json schema 
    record.setMappingId(EXAMPLE_SCHEMA_ID_JSON);

    mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

    recordFile = new MockMultipartFile("record", "record_json.json", "application/json", mapper.writeValueAsString(record).getBytes());
    mappingFile = new MockMultipartFile("document", EXAMPLE_SCHEMA_ID_JSON + "4gemma.mapping", "application/json", mappingContent.getBytes());

    Assert.assertEquals(1, mappingsDir.list().length);
    this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
            file(recordFile).
            file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andDo(document("post-json-mapping", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
    Assert.assertEquals(2, mappingsDir.list().length);

    // list all mappings 
    this.mockMvc.perform(get("/api/v1/mapping/")).andExpect(status().isOk()).andDo(document("get-all-mappings", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andReturn().getResponse();

    this.mockMvc.perform(get("/api/v1/mapping/").param("page", Integer.toString(0)).param("size", Integer.toString(20))).andExpect(status().isOk()).andDo(document("get-all-mappings-pagination", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andReturn().getResponse();

    // Get single mapping record
    String etag = this.mockMvc.perform(get("/api/v1/mapping/" + EXAMPLE_SCHEMA_ID_JSON + "/" + GEMMA.name()).accept(MappingRecord.MAPPING_RECORD_MEDIA_TYPE.toString())).andExpect(status().isOk()).andDo(document("get-single-mapping", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andReturn().getResponse().getHeader("ETag");

    // Get mapping file
    this.mockMvc.perform(get("/api/v1/mapping/" + EXAMPLE_SCHEMA_ID_JSON + "/" + GEMMA.name())).andExpect(status().isOk()).andDo(document("get-mapping-file", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andReturn().getResponse();

    //update schema document and create new version
    mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);
    mappingFile = new MockMultipartFile("document", EXAMPLE_SCHEMA_ID_JSON + "4gemma_v2.mapping", "application/json", mappingContent.getBytes());
    etag = this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/" + EXAMPLE_SCHEMA_ID_JSON + "/" + GEMMA.name()).
            file(recordFile).
            file(mappingFile).header("If-Match", etag).with(putMultipart())).
            andExpect(status().isOk()).
            andDo(document("update-mapping", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
            andReturn().getResponse().getHeader("ETag");

    // Get mapping file version 2
    this.mockMvc.perform(get("/api/v1/mapping/" + EXAMPLE_SCHEMA_ID_JSON + "/" + GEMMA.name())).andExpect(status().isOk()).andDo(document("get-mapping-filev2", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andReturn().getResponse();

  }

  private static RequestPostProcessor putMultipart() { // it's nice to extract into a helper
    return (MockHttpServletRequest request) -> {
      request.setMethod("PUT");
      return request;
    };
  }

}
