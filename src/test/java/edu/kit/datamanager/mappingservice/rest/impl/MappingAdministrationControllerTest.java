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
package edu.kit.datamanager.mappingservice.rest.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.AclEntry;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.restdocs.RestDocumentationContextProvider;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 */
@ExtendWith({SpringExtension.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ComponentScan("edu.kit.datamanager.mappingservice")
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
public class MappingAdministrationControllerTest {

    private final static String TEMP_DIR_4_MAPPING = "/tmp/mapping-service/";
    private static final String MAPPING_ID = "my_dc";
    private static final String MAPPING_TYPE = "GEMMA_v1.0.0";
    private static final String MAPPING_TITLE = "TITLE";
    private static final String MAPPING_DESCRIPTION = "DESCRIPTION";

    private final static Logger LOGGER = LoggerFactory.getLogger(MappingAdministrationControllerTest.class);

    @RegisterExtension
    final RestDocumentationExtension restDocumentation = new RestDocumentationExtension("custom");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private IMappingRecordDao mappingRecordDao;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        mappingRecordDao.deleteAll();
        try {
            try (Stream<Path> walk = Files.walk(Paths.get(URI.create("file://" + TEMP_DIR_4_MAPPING)))) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            if(Paths.get(TEMP_DIR_4_MAPPING).toFile().mkdir()){
                LOGGER.trace("Successfully created temporary mapping directory.");
            }else{
                LOGGER.error("Failed to create temporary mapping directory.");
            }
        } catch (IOException ex) {
           LOGGER.info("Failed to setup temporary mapping directory.", ex);
        }
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .uris().withPort(8095)
                        .and().operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(Preprocessors.modifyHeaders().
                                remove("X-Content-Type-Options").
                                remove("X-XSS-Protection").
                                remove("X-Frame-Options"), prettyPrint()))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
                .build();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingWithoutID() throws Exception {
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        MappingRecord record = new MappingRecord();
        record.setMappingId(null);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("test2", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingWithWrongID() throws Exception {
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        MappingRecord record = new MappingRecord();
        record.setMappingId("");
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("test2", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isBadRequest());

        record.setMappingId("");

        recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isBadRequest());

        record.setMappingId(" ");

        recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isBadRequest());

        record.setMappingId("\t");

        recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isBadRequest());

        record.setMappingId("    ");

        recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isBadRequest());
    }

    /**
     * Test of createMapping method of class MappingAdministrationController.
     */
    @Test
    public void testCreateMapping() throws Exception {
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        MappingRecord record = new MappingRecord();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("test2", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).
                andDo(print()).
                andExpect(status().isCreated()).
                andExpect(redirectedUrlPattern("http://*:*/api/v1/mappingAdministration/*")).
                andReturn();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingNoRecord() throws Exception {
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingEmptyRecord() throws Exception {
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", "".getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testCreateMappingNoMapping() throws Exception {
        MappingRecord record = new MappingRecord();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("test2", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testCreateMappingWrongRecord() throws Exception {
        String mappingContent = "";
        MappingRecord record = new MappingRecord();
        record.setMappingId(null);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(null);
        record.setDescription(null);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(null);
        recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingTwice() throws Exception {
        testCreateMapping();
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        MappingRecord record = new MappingRecord();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isConflict()).andReturn();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingWithAcl() throws Exception {
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        MappingRecord record = new MappingRecord();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("test2", PERMISSION.ADMINISTRATE));
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        record.setAcl(aclEntries);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mappingAdministration/*")).andReturn();
    }

    /**
     * Test of getMappingById method, of class MappingAdministrationController.
     */
    @Test
    public void testGetMappingById() throws Exception {
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        ObjectMapper map = new ObjectMapper();
        MappingRecord result = map.readValue(res.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(result);
        assertEquals(mappingId, result.getMappingId());
        assertEquals(MAPPING_TYPE, result.getMappingType());
        assertEquals(MAPPING_TITLE, result.getTitle());
        assertEquals(MAPPING_DESCRIPTION, result.getDescription());
        assertTrue(result.getMappingDocumentUri().contains(getMappingIdUrl));
    }

    /**
     * Test of getMappingById method, of class MappingAdministrationController.
     */
    @Test
    public void testGetMappingByIdWithInvalidMapping() throws Exception {
        testCreateMapping();
        String mappingId = "invalidMappingId";
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    }

    /**
     * Test of getMappingById method, of class MappingAdministrationController.
     */
    @Test
    public void testGetMappingDocumentById() throws Exception {
        String expResult = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        testCreateMapping();
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID + "/document";
        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isOk()).andReturn();
        String result = res.getResponse().getContentAsString();
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMappingDocumentByIdWithInvalidMapping() throws  Exception {
        testCreateMapping();
        String mappingId = "invalidMappingId";
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMapping() throws Exception {
        testCreateMapping();

        String getMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("somebody", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);

        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isOk()).andReturn();

        ObjectMapper map = new ObjectMapper();
        MappingRecord resultRecord = map.readValue(result.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(resultRecord);
        assertEquals(MAPPING_ID, resultRecord.getMappingId());
        assertEquals(MAPPING_TYPE, resultRecord.getMappingType());
        assertTrue(resultRecord.getMappingDocumentUri().contains(putMappingIdUrl));
        result = this.mockMvc.perform(get(resultRecord.getMappingDocumentUri())).andDo(print()).andExpect(status().isOk()).andReturn();
        String newMapping = result.getResponse().getContentAsString();
        assertNotNull(newMapping);
        assertEquals(mappingContent, newMapping);
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithoutDocument() throws Exception {
        testCreateMapping();
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("somebody", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isOk()).andReturn();

        ObjectMapper map = new ObjectMapper();
        MappingRecord resultRecord = map.readValue(result.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(resultRecord);
        assertEquals(MAPPING_ID, resultRecord.getMappingId());
        assertEquals(MAPPING_TYPE, resultRecord.getMappingType());
        assertTrue(resultRecord.getMappingDocumentUri().contains(putMappingIdUrl));
        result = this.mockMvc.perform(get(resultRecord.getMappingDocumentUri())).andDo(print()).andExpect(status().isOk()).andReturn();
        String oldMapping = result.getResponse().getContentAsString();
        assertNotNull(oldMapping);
        assertEquals(mappingContent, oldMapping);
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithoutRecord() throws Exception {
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    @Disabled("Unclear expected behaviour..ignore test for the moment")
    public void testUpdateMappingWithWrongRecord1() throws Exception {
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        record.setMappingId("something");
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testUpdateMappingWithWrongRecord3() throws Exception {
        testCreateMapping();
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + "unknownMapping";
        this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isNotFound()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithInvalidRecord() throws Exception {
        System.out.println("testUpdateMappingWithInvalidRecord");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        record.setMappingId(null);
        record.setMappingType(null);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
            this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                    file(recordFile).
                    file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithInvalidRecord2() throws Exception {
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        record.setMappingDocumentUri("/tmp/invalid/path/to/document");
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isOk()).andReturn();
        ObjectMapper map = new ObjectMapper();
        MappingRecord resultRecord = map.readValue(result.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(resultRecord);
        assertEquals(mappingId, resultRecord.getMappingId());
        assertEquals(MAPPING_TYPE, resultRecord.getMappingType());
        assertTrue(resultRecord.getMappingDocumentUri().contains(putMappingIdUrl));
        result = this.mockMvc.perform(get(resultRecord.getMappingDocumentUri())).andDo(print()).andExpect(status().isOk()).andReturn();
        String newMapping = result.getResponse().getContentAsString();
        assertNotNull(newMapping);
        assertEquals(mappingContent, newMapping);
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithWrongEtag() throws Exception {
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = "wrongEtag";
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("somebody", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isPreconditionFailed()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithoutEtag() throws Exception {
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("somebody", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).with(putMultipart())).andDo(print()).andExpect(status().isPreconditionRequired()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMapping() throws Exception {
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();
        String expectedFilename = mappingId + "_" + MAPPING_TYPE + ".mapping";
        String[] listing = mappingsDir.list();
        assertNotEquals(expectedFilename, listing != null ? listing[0] : null);
        this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isNotFound()).andReturn();
        assertEquals(0, mappingRecordDao.count());
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMappingUnknownMappingId() throws Exception {
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + "unknownMappingId";
        this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();
        String expectedFilename = mappingId + "_" + MAPPING_TYPE + ".mapping";
        assertEquals("my_dc_" + MAPPING_TYPE + ".mapping", expectedFilename);
        assertEquals(1, mappingRecordDao.count());
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMappingMissingEtag() throws Exception {
        System.out.println("testDeleteMappingMissingEtag");
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(delete(deleteMappingIdUrl)).andDo(print()).andExpect(status().isPreconditionRequired()).andReturn();
        String expectedFilename = mappingId + "_" + MAPPING_TYPE + ".mapping";
        assertEquals("my_dc_" + MAPPING_TYPE + ".mapping", expectedFilename);
        this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(1, mappingRecordDao.count());
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMappingWrongEtag() throws Exception {
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        String mappingId = MAPPING_ID;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = "somethingTotallyWrong";

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isPreconditionFailed()).andReturn();
        String expectedFilename = mappingId + "_" + MAPPING_TYPE + ".mapping";
        assertEquals("my_dc_" + MAPPING_TYPE + ".mapping", expectedFilename);
        this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(1, mappingRecordDao.count());
    }

    private static RequestPostProcessor putMultipart() { // it's nice to extract into a helper
        return (MockHttpServletRequest request) -> {
            request.setMethod("PUT");
            return request;
        };
    }

}
