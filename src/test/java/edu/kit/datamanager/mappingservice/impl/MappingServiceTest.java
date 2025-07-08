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
package edu.kit.datamanager.mappingservice.impl;

import edu.kit.datamanager.mappingservice.MappingServiceApplication;
import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.exception.MappingNotFoundException;
import edu.kit.datamanager.mappingservice.exception.MappingServiceException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.springframework.web.client.ResourceAccessException;

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
//@TestPropertySource(properties = {"server.port=41300"})
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:db_doc;DB_CLOSE_DELAY=-1"})
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MappingServiceTest {
    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    IMappingRecordDao mappingRepo;

    @Autowired
    MappingService mappingService4Test;

    @Autowired
    MeterRegistry meterRegistry;

    private final static String TEMP_DIR_4_MAPPING = "/tmp/mapping-service/";

    @BeforeEach
    public void setUp() {
        try {
            try (Stream<Path> walk = Files.walk(Paths.get(URI.create("file://" + TEMP_DIR_4_MAPPING)))) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            Paths.get(TEMP_DIR_4_MAPPING).toFile().mkdirs();
        } catch (IOException ex) {
            fail("IOException occurred while creating temp directory.", ex);
        }
        mappingRepo.deleteAll();
    }

    @Test
    public void testConstructor() {
        new MappingService(applicationProperties, meterRegistry);
    }

    @Test
    @Disabled(value = "Deprecated URL constructor")
    public void testConstructorRelativePath() throws IOException {
        try {
            URL relativePath = new URL("file:tmp/relativePath");
            ApplicationProperties ap = new ApplicationProperties();
            ap.setMappingsLocation(relativePath);
            File file = new File(relativePath.getPath());
            assertFalse(file.exists());
            new MappingService(ap, meterRegistry);
            assertTrue(file.exists());
            FileUtils.deleteDirectory(file);
            assertFalse(file.exists());
        } catch (MappingException ie) {
            fail();
        }
    }

    @Test
    public void testConstructorFailing() {
        try {
            new MappingService(null, meterRegistry);
            fail("Expected MappingServiceException");
        } catch (MappingServiceException ie) {
            assertTrue(true);
        }
    }

    /**
     * Test of updateMapping method, of class MappingService.
     */
    @Test
    public void testUpdateMappingWithoutCreate() {
        System.out.println("updateMapping");
        String content = "new content";
        String mappingId = "mappingId";
        String mappingType = "TEST_0.0.0";
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

        try {
            mappingService4Test.updateMapping(content, mappingRecord);
            fail();
        } catch (IOException | MappingNotFoundException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("Mapping"));
            assertTrue(ie.getMessage().contains("doesn't exist"));
            assertTrue(ie.getMessage().contains(mappingType));
        }
    }

//    /**
//     * Test of deleteMapping method, of class MappingService.
//     */
//    @Test
//    public void testDeleteMapping() {
//        System.out.println("deleteMapping");
//        String content = "content";
//        assertEquals(0, mappingRepo.count());
//        testCreateMapping();
//        assertEquals(1, mappingRepo.count());
//        MappingRecord mappingRecord = mappingRepo.findAll().get(0);
//

    /// /    MappingService instance = new MappingService(applicationProperties);
//        URL mappingsLocation = applicationProperties.getMappingsLocation();
//        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
//        assertEquals(1, mappingsDir.list().length);
//        File mappingFile = new File(mappingRecord.getMappingDocumentUri());
//        try {
//            assertTrue(mappingFile.exists());
//            assertEquals(mappingFile.getName(), mappingsDir.list()[0]);
//
//            mappingService4Test.deleteMapping(mappingRecord);
//            assertEquals(1, mappingsDir.list().length);
//            assertFalse(mappingFile.exists());
//
//            File mappingFileMarkedAsDeleted = Paths.get(mappingsDir.getAbsolutePath(), mappingsDir.list()[0]).toFile();
//            assertTrue(mappingFileMarkedAsDeleted.getName().contains(mappingFile.getName()));
//            assertTrue(mappingFileMarkedAsDeleted.exists());
//            assertEquals(content, FileUtils.readFileToString(mappingFileMarkedAsDeleted, StandardCharsets.UTF_8));
//            assertEquals(0, mappingRepo.count());
//        } catch (IOException | MappingException ie) {
//            fail();
//        }
//    }
    @Test
    public void testDeleteNotExistingMapping() {
        System.out.println("createMapping");
        String mappingId = "mappingId";
        String mappingType = "GEMMA_unknown";
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

        try {
            mappingService4Test.deleteMapping(mappingRecord);
        } catch (MappingException ie) {
            fail("deleteMapping() should never fail.");
        }
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithWrongMappingId() {
        System.out.println("testExecuteMappingWithWrongMappingId");
        URI contentUrl;
        String mappingId = "unknownMapping";
        try {
            mappingService4Test.executeMapping(null, mappingId);
            fail("Exception expected!");
        } catch (MappingException | MappingPluginException ie) {
            assertTrue(ie.getMessage().contains("Either contentUrl"));
        }
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        contentUrl = srcFile.toURI();
        try {
            mappingService4Test.executeMapping(contentUrl, mappingId);
            fail("MappingNotFoundException expected.");
        } catch (MappingPluginException | MappingNotFoundException e) {
            //exception received
        }
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithWrongURI() {
        System.out.println("testExecuteMappingWithWrongURI");
        String mappingId = "unknownMapping";
        String contentUrl = "file:///unknown/location/of/content";
        try {
            URI url = new URI(contentUrl);
            mappingService4Test.executeMapping(url, mappingId);
            fail("Expected MappingNotFoundException");
        } catch (MappingNotFoundException e) {
            //got expected exception
        } catch (MappingException | MappingPluginException ie) {
            fail("Expected MappingNotFoundException but received MappingException | MappingPluginException");
        } catch (ResourceAccessException ex) {
            //happens under Windows...no idea why not under Unix
        } catch (URISyntaxException ex) {
            fail("Got URISyntaxException while creating URI from " + contentUrl);
        }
    }

    @Test
    public void testExecuteMappingWithoutExistingMapping() {
        System.out.println("executeMapping");
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        URI contentUrl = srcFile.toURI();
        String mappingId = "GEMMA_unknown";
        try {
            mappingService4Test.executeMapping(contentUrl, mappingId);
            fail("Expected MappingNotFoundException");
        } catch (MappingNotFoundException e) {
            //received proper exception
        } catch (MappingPluginException e) {
            fail("Got MappingPluginException, expected MappingNotFoundException");
        }
    }
}
