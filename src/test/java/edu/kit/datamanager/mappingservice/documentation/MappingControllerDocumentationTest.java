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
package edu.kit.datamanager.mappingservice.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.datamanager.mappingservice.MappingServiceApplication;
import edu.kit.datamanager.mappingservice.dao.IAclEntryDao;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
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
import java.util.stream.Stream;

import static edu.kit.datamanager.mappingservice.mapping.Mapping.GEMMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 *
 */
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@EnableRuleMigrationSupport
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = MappingServiceApplication.class)
//RANDOM_PORT)
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
    private IMappingRecordDao mappingRecordDao;
    @Autowired
    private IAclEntryDao aclEntryDao;

    private final static String EXAMPLE_SCHEMA_ID_XML = "my_first_xsd";
    private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/restdocu/";
    private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";
    private final static String EXAMPLE_SCHEMA_ID_JSON = "my_first_json";

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
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
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .uris().withPort(8095)
                        .and().operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(Preprocessors.removeHeaders("X-Content-Type-Options", "X-XSS-Protection", "X-Frame-Options"), prettyPrint()))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
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
        ObjectMapper mapper = new ObjectMapper();

        MockMultipartFile recordFile = new MockMultipartFile("record", "record_xml.json", "application/json", mapper.writeValueAsString(record).getBytes());
        MockMultipartFile mappingFile = new MockMultipartFile("document", EXAMPLE_SCHEMA_ID_XML + "4gemma.mapping", "application/json", mappingContent.getBytes());

        assertEquals(0, mappingsDir.list().length);
//    assertNotNull(this.context);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
                        file(recordFile).
                        file(mappingFile)).
                andDo(print()).
                andExpect(status().isCreated()).
                andDo(document("post-xml-mapping", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).
                andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).
                andReturn();
        assertEquals(1, mappingsDir.list().length);
        // register a second mapping for json schema
        record.setMappingId(EXAMPLE_SCHEMA_ID_JSON);

        mappingContent = FileUtils.readFileToString(new File("src/test/resources/mapping/gemma/simple.mapping"), StandardCharsets.UTF_8);

        recordFile = new MockMultipartFile("record", "record_json.json", "application/json", mapper.writeValueAsString(record).getBytes());
        mappingFile = new MockMultipartFile("document", EXAMPLE_SCHEMA_ID_JSON + "4gemma.mapping", "application/json", mappingContent.getBytes());

        assertEquals(1, mappingsDir.list().length);
        this.mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/mapping/").
                file(recordFile).
                file(mappingFile)).andDo(print()).andExpect(status().isCreated()).andDo(document("post-json-mapping", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).andExpect(redirectedUrlPattern("http://*:*//api/v1/mapping/" + record.getMappingId() + "/" + record.getMappingType())).andReturn();
        assertEquals(2, mappingsDir.list().length);

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
