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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.entities.PERMISSION;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.AclEntry;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
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

import static org.junit.jupiter.api.Assertions.*;
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
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@EnableRuleMigrationSupport
@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = MappingServiceApplication.class)
//RANDOM_PORT)
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        WithSecurityContextTestExecutionListener.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {"server.port=41300"})
@TestPropertySource(properties = {"mapping-service.mappings-location=/tmp/mapping-service"})
@TestPropertySource(properties = {"mapping-service.python-location=/usr/bin/python3"})
public class MappingAdministrationControllerTest {

    private final static String TEMP_DIR_4_MAPPING = "/tmp/mapping-service/";
    private static final String MAPPING_ID = "my_dc";
    private static final String MAPPING_TYPE = "GEMMA";
    private static final String MAPPING_TITLE = "TITEL";
    private static final String MAPPING_DESCRIPTION = "DESCRIPTION";

    private MockMvc mockMvc;

    @Autowired
    private IMappingRecordDao mappingRecordDao;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
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
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .uris().withPort(8095)
                        .and().operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(Preprocessors.removeHeaders("X-Content-Type-Options", "X-XSS-Protection", "X-Frame-Options"), prettyPrint()))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
                .build();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMapping() throws Exception {
        System.out.println("createMapping");
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
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


        assertEquals(0, mappingsDir.listFiles().length);

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                        file(recordFile).
                        file(mappingFile)).
                andDo(print()).
                andExpect(status().isCreated()).
                andExpect(redirectedUrlPattern("http://*:*/api/v1/mappingAdministration")).
                andReturn();

        System.out.println(mappingsDir.getAbsolutePath());
        assertEquals(1, mappingsDir.listFiles().length);
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingNoRecord() throws Exception {
        System.out.println("testCreateMappingNoRecord");
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of createMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testCreateMappingEmptyRecord() throws Exception {
        System.out.println("testCreateMappingEmptyRecord");
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", "".getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testCreateMappingNoMapping() throws Exception {
        System.out.println("testCreateMappingNoMapping");
        MappingRecord record = new MappingRecord();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF",PERMISSION.READ));
        aclEntries.add(new AclEntry("test2",PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile)).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    public void testCreateMappingWrongRecord() throws Exception {
        System.out.println("testCreateMappingEmptyMapping");
        String mappingContent = "";
        MappingRecord record = new MappingRecord();
        record.setMappingId(null);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(null);
        record.setDescription(null);
        Set<AclEntry> aclEntries = new HashSet<>();
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
        System.out.println("testCreateMappingTwice");
        testCreateMapping();
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        MappingRecord record = new MappingRecord();
        record.setMappingId(MAPPING_ID);
        record.setMappingType(MAPPING_TYPE);
        record.setTitle(MAPPING_TITLE);
        record.setDescription(MAPPING_DESCRIPTION);
        Set<AclEntry> aclEntries = new HashSet<>();
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
        System.out.println("testCreateMappingWithAcl");
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
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

        assertEquals(0, mappingsDir.list().length);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mappingAdministration")).andReturn();
        assertEquals(1, mappingsDir.list().length);
    }

    /**
     * Test of getMappingById method, of class MappingAdministrationController.
     */
    @Test
    public void testGetMappingById() throws UnsupportedEncodingException, JsonProcessingException, Exception {
        System.out.println("getMappingById");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        ObjectMapper map = new ObjectMapper();
        MappingRecord result = map.readValue(res.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(result);
        assertEquals(mappingId, result.getMappingId());
        assertEquals(mappingType, result.getMappingType());
        assertEquals(MAPPING_TITLE, result.getTitle());
        assertEquals(MAPPING_DESCRIPTION, result.getDescription());
        assertTrue(result.getMappingDocumentUri().contains(getMappingIdUrl));
    }

    /**
     * Test of getMappingById method, of class MappingAdministrationController.
     */
    @Test
    public void testGetMappingByIdWithInvalidMapping() throws Exception {
        System.out.println("testGetMappingByIdWithInvalidMapping");
        testCreateMapping();
        String mappingId = "invalidMappingId";
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isNotFound()).andReturn();
   }

    /**
     * Test of getMappingById method, of class MappingAdministrationController.
     */
    @Test
    public void testGetMappingDocumentById() throws UnsupportedEncodingException, JsonProcessingException, Exception {
        System.out.println("testGetMappingDocumentById");
        String expResult = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);
        testCreateMapping();
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + MAPPING_ID;
        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isOk()).andReturn();
        String result = res.getResponse().getContentAsString();
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMappingDocumentByIdWithInvalidMapping() throws UnsupportedEncodingException, JsonProcessingException, Exception {
        System.out.println("testGetMappingDocumentByIdWithInvalidMapping");
        testCreateMapping();
        String mappingId = "invalidMappingId";
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isNotFound()).andReturn();
    }

//    /**
//     * Test of getMappings method, of class MappingAdministrationController.
//     */
//    @Test
//    public void testGetMappings() throws UnsupportedEncodingException, JsonProcessingException, Exception {
//        System.out.println("getMappings");
//        create2Mappings();
//        String mappingId = MAPPING_ID;
//        String mappingType = MAPPING_TYPE;
//        String getMappingIdUrl = "/api/v1/mappingAdministration/";
//        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl)).andDo(print()).andExpect(status().isOk()).andReturn();
//        ObjectMapper map = new ObjectMapper();
//        MappingRecord[] result = map.readValue(res.getResponse().getContentAsString(), MappingRecord[].class);
//        assertNotNull(result);
//        assertEquals(2, result.length);
//        for (MappingRecord item : result) {
//            assertEquals(mappingId, item.getMappingId());
//            assertTrue(item.getMappingDocumentUri().contains(mappingId));
//        }
//    }

//    /**
//     * Test of getMappings method, of class MappingAdministrationController.
//     */
//    @Test
//    public void testGetMappingsWithFilter() throws UnsupportedEncodingException, JsonProcessingException, Exception {
//        System.out.println("getMappings");
//        create2Mappings();
//        String mappingId = MAPPING_ID;
//        String mappingType = MAPPING_TYPE;
//        String getMappingIdUrl = "/api/v1/mappingAdministration/";
//        MvcResult res = this.mockMvc.perform(get(getMappingIdUrl).param("mappingType", MAPPING_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
//        ObjectMapper map = new ObjectMapper();
//        MappingRecord[] result = map.readValue(res.getResponse().getContentAsString(), MappingRecord[].class);
//        assertNotNull(result);
//        assertEquals(2, result.length);
//        for (MappingRecord item : result) {
//            assertEquals(mappingType, item.getMappingType());
//            assertTrue(item.getMappingDocumentUri().contains(mappingType));
//        }
//        res = this.mockMvc.perform(get(getMappingIdUrl).param("mappingId", MAPPING_ID)).andDo(print()).andExpect(status().isOk()).andReturn();
//        map = new ObjectMapper();
//        result = map.readValue(res.getResponse().getContentAsString(), MappingRecord[].class);
//        assertNotNull(result);
//        assertEquals(1, result.length);
//        for (MappingRecord item : result) {
//            assertEquals(MAPPING_ID, item.getMappingId());
//            assertEquals(mappingType, item.getMappingType());
//            assertTrue(item.getMappingDocumentUri().contains(mappingType));
//        }
//
//    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMapping() throws JsonProcessingException, Exception {
        System.out.println("updateMapping");
        testCreateMapping();
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("someoneelse", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(1, mappingsDir.list().length);
        ObjectMapper map = new ObjectMapper();
        MappingRecord resultRecord = map.readValue(result.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(resultRecord);
        assertEquals(mappingId, resultRecord.getMappingId());
        assertEquals(mappingType, resultRecord.getMappingType());
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
    public void testUpdateMappingWithoutDocument() throws JsonProcessingException, Exception {
        System.out.println("updateMapping");
        testCreateMapping();
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("someoneelse", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(1, mappingsDir.list().length);
        ObjectMapper map = new ObjectMapper();
        MappingRecord resultRecord = map.readValue(result.getResponse().getContentAsString(), MappingRecord.class);
        assertNotNull(resultRecord);
        assertEquals(mappingId, resultRecord.getMappingId());
        assertEquals(mappingType, resultRecord.getMappingType());
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
    public void testUpdateMappingWithoutRecord() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithoutRecord");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithWrongRecord1() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithWrongRecord1");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        record.setMappingId("somethingelse");
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

//    @Test
//    public void testUpdateMappingWithWrongRecord2() throws JsonProcessingException, Exception {
//        System.out.println("testUpdateMappingWithWrongRecord2");
//        testCreateMapping();
//        String mappingId = MAPPING_ID;
//        String mappingType = MAPPING_TYPE;
//        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
//        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
//        String etag = result.getResponse().getHeader("ETag");
//        String body = result.getResponse().getContentAsString();
//
//        ObjectMapper mapper = new ObjectMapper();
//        MappingRecord record = mapper.readValue(body, MappingRecord.class);
//        record.setMappingType("somethingelse");
//        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);
//
//        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
//        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
//        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
//        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
//                file(recordFile).
//                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
//    }

    @Test
    public void testUpdateMappingWithWrongRecord3() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithWrongRecord3");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + "unknownMaping";
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

//    @Test
//    public void testUpdateMappingWithWrongRecord4() throws JsonProcessingException, Exception {
//        System.out.println("testUpdateMappingWithWrongRecord4");
//        testCreateMapping();
//        String mappingId = MAPPING_ID;
//        String mappingType = MAPPING_TYPE;
//        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId + "/" + mappingType;
//        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
//        String etag = result.getResponse().getHeader("ETag");
//        String body = result.getResponse().getContentAsString();
//
//        ObjectMapper mapper = new ObjectMapper();
//        MappingRecord record = mapper.readValue(body, MappingRecord.class);
//        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);
//
//        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
//        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
//        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId + "/" + "unknownType";
//        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
//                file(recordFile).
//                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
//    }


    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithInvalidRecord() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithInvalidRecord");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
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
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isBadRequest()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithInvalidRecord2() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithInvalidRecord2");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
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
        assertEquals(mappingType, resultRecord.getMappingType());
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
    public void testUpdateMappingWithWrongEtag() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithWrongEtag");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = "wrongEtag";
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("someoneelse", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).header("If-Match", etag).with(putMultipart())).andDo(print()).andExpect(status().isPreconditionFailed()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testUpdateMappingWithoutEtag() throws JsonProcessingException, Exception {
        System.out.println("testUpdateMappingWithoutEtag");
        testCreateMapping();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = "notUsed";
        String body = result.getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        MappingRecord record = mapper.readValue(body, MappingRecord.class);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(new AclEntry("SELF", PERMISSION.READ));
        aclEntries.add(new AclEntry("someoneelse", PERMISSION.ADMINISTRATE));
        record.setAcl(aclEntries);
        String mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);

        MockMultipartFile recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());
        String putMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(putMappingIdUrl).
                file(recordFile).
                file(mappingFile).with(putMultipart())).andDo(print()).andExpect(status().isPreconditionRequired()).andReturn();
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMapping() throws JsonProcessingException, Exception {
        System.out.println("testDeleteMapping");
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isNoContent()).andReturn();
        assertEquals(1, mappingsDir.list().length);
        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
        assertNotEquals(expectedFilename, mappingsDir.list()[0]);
        result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isNotFound()).andReturn();
        assertEquals(0, mappingRecordDao.count());
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMappingUnknownMappingId() throws JsonProcessingException, Exception {
        System.out.println("testDeleteMappingUnknownMappingId");
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + "unknownMappingId";
        result = this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isNotFound()).andReturn();
        assertEquals(1, mappingsDir.list().length);
        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
        assertEquals("mappingSchemaTests", mappingsDir.list()[0]);
        assertEquals(1, mappingRecordDao.count());
    }

//    /**
//     * Test of updateMapping method, of class MappingAdministrationController.
//     */
//    @Test
//    public void testDeleteMappingUnknownMappingType() throws JsonProcessingException, Exception {
//        System.out.println("testDeleteMappingUnknownMappingType");
//        testCreateMapping();
//        assertEquals(1, mappingRecordDao.count());
//        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
//        String mappingId = MAPPING_ID;
//        String mappingType = MAPPING_TYPE;
//        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
//        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
//        String etag = result.getResponse().getHeader("ETag");
//
//        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
//        result = this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isNotFound()).andReturn();
//        assertEquals(1, mappingsDir.list().length);
//        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
//        assertEquals(expectedFilename, mappingsDir.list()[0]);
//        assertEquals(1, mappingRecordDao.count());
//    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMappingMissingEtag() throws JsonProcessingException, Exception {
        System.out.println("testDeleteMappingMissingEtag");
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = result.getResponse().getHeader("ETag");

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(delete(deleteMappingIdUrl)).andDo(print()).andExpect(status().isPreconditionRequired()).andReturn();
        assertEquals(1, mappingsDir.list().length);
        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
        assertEquals("mappingSchemaTests", mappingsDir.list()[0]);
        result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(1, mappingRecordDao.count());
    }

    /**
     * Test of updateMapping method, of class MappingAdministrationController.
     */
    @Test
    public void testDeleteMappingWrongEtag() throws JsonProcessingException, Exception {
        System.out.println("testDeleteMappingWrongEtag");
        testCreateMapping();
        assertEquals(1, mappingRecordDao.count());
        File mappingsDir = Paths.get(TEMP_DIR_4_MAPPING).toFile();
        String mappingId = MAPPING_ID;
        String mappingType = MAPPING_TYPE;
        String getMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        MvcResult result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        String etag = "somethingTotallyWrong";

        String deleteMappingIdUrl = "/api/v1/mappingAdministration/" + mappingId;
        result = this.mockMvc.perform(delete(deleteMappingIdUrl).header("If-Match", etag)).andDo(print()).andExpect(status().isPreconditionFailed()).andReturn();
        assertEquals(1, mappingsDir.list().length);
        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
        assertEquals("mappingSchemaTests", mappingsDir.list()[0]);
        result = this.mockMvc.perform(get(getMappingIdUrl).header("Accept", MappingRecord.MAPPING_RECORD_MEDIA_TYPE)).andDo(print()).andExpect(status().isOk()).andReturn();
        assertEquals(1, mappingRecordDao.count());
    }

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

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mappingAdministration/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();

        mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple_v2.mapping"), StandardCharsets.UTF_8);
        record.setMappingId("anotherMappingId");

        recordFile = new MockMultipartFile("record", "record.json", "application/json", mapper.writeValueAsString(record).getBytes());
        mappingFile = new MockMultipartFile("document", "my_dc4gemma.mapping", "application/json", mappingContent.getBytes());

        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mappingAdministration/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andExpect(redirectedUrlPattern("http://*:*//api/v1/mappingAdministration/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
    }

    private static RequestPostProcessor putMultipart() { // it's nice to extract into a helper
        return (MockHttpServletRequest request) -> {
            request.setMethod("PUT");
            return request;
        };
    }

}
