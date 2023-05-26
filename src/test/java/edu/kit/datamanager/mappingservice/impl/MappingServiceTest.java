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
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
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
@TestPropertySource(properties = {"metastore.indexer.mappingsLocation=file:///tmp/metastore2/mapping"})
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MappingServiceTest {

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    IMappingRecordDao mappingRepo;

    @Autowired
    MappingService mappingService4Test;

    private final static String TEMP_DIR_4_MAPPING = "/tmp/mapping-service/";

    @BeforeEach
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

    @Test
    public void testConstructor() throws URISyntaxException {
        new MappingService(applicationProperties);
    }

    @Test
    public void testConstructorRelativePath() throws IOException, URISyntaxException {
        try {
            URL relativePath = new URL("file:tmp/relativePath");
            ApplicationProperties ap = new ApplicationProperties();
            ap.setMappingsLocation(relativePath);
            File file = new File(relativePath.getPath());
            assertFalse(file.exists());
            new MappingService(ap);
            assertTrue(file.exists());
            FileUtils.deleteDirectory(file);
            assertFalse(file.exists());
        } catch (MappingException ie) {
            fail();
        }
    }

    @Test
    public void testConstructorFailing() throws IOException, URISyntaxException {
        try {
            new MappingService(null);
            fail();
        } catch (MappingException ie) {
            assertTrue(true);
        }
        //seems to be no problem under Windows and if run as root this is also no issue, so let's skip this test for the moment.
//        try {
//            ApplicationProperties ap = new ApplicationProperties();
//            ap.setMappingsLocation(new URL("file:///forbidden"));
//            new MappingService(ap);
//            fail();
//        } catch (MappingException ie) {
//            assertTrue(true);
//        }
    }

//    /**
//     * Test of createMapping method, of class MappingService.
//     */
//    @Test
//    public void testCreateMapping() {
//        System.out.println("createMapping");
//        String content = "content";
//        String mappingId = "mappingId";
//        String mappingType = "GEMMA_unknown";
//        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
//        MappingRecord mappingRecord = new MappingRecord();
//        mappingRecord.setMappingId(mappingId);
//        mappingRecord.setMappingType(mappingType);
//
////    MappingService instance = new MappingService(applicationProperties);
//        URL mappingsLocation = applicationProperties.getMappingsLocation();
//        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
//        assertEquals(0, Objects.requireNonNull(mappingsDir.list()).length);
//        try {
//            mappingService4Test.createMapping(content, mappingRecord);
//            assertEquals(1, mappingsDir.list().length);
//            assertEquals(expectedFilename, mappingsDir.list()[0]);
//            File mappingFile = Paths.get(mappingsDir.getAbsolutePath(), expectedFilename).toFile();
//            assertEquals(content, FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8));
//        } catch (IOException | MappingException ie) {
//            fail();
//        }
//    }
//    /**
//     * Test of createMapping method, of class MappingService.
//     */
//    @Test
//    public void testCreateMappingTwice() {
//        System.out.println("createMapping");
//        String content = "content";
//        String newContent = "newContent";
//        String mappingId = "mappingId";
//        String mappingType = "GEMMA_unknown";
//        MappingRecord mappingRecord = new MappingRecord();
//        mappingRecord.setMappingId(mappingId);
//        mappingRecord.setMappingType(mappingType);
//
////    MappingService instance = new MappingService(applicationProperties);
//        URL mappingsLocation = applicationProperties.getMappingsLocation();
//        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
//        System.out.println(mappingsLocation.getPath());
//        for (String i : mappingsDir.list()) System.out.println(i);
//        assertEquals(0, mappingsDir.list().length);
//        try {
//            mappingService4Test.createMapping(content, mappingRecord);
//            mappingService4Test.createMapping(newContent, mappingRecord);
//            fail();
//        } catch (IOException | MappingException ie) {
//            assertTrue(true);
//            assertTrue(ie.getMessage().contains("already exists"));
//            assertTrue(ie.getMessage().contains(mappingType));
//        }
//    }
//    /**
//     * Test of createMapping method, of class MappingService.
//     */
//    @Test
//    public void testCreateMappingWithWrongMapping() {
//        System.out.println("createMapping");
//        String content = "content";
//        String mappingId = "mappingId";
//        String mappingType = "aTotallyUnknownMapping";
//        MappingRecord mappingRecord = new MappingRecord();
//        mappingRecord.setMappingId(mappingId);
//        mappingRecord.setMappingType(mappingType);
//
////    MappingService instance = new MappingService(applicationProperties);
//        URL mappingsLocation = applicationProperties.getMappingsLocation();
//        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
//        assertEquals(0, mappingsDir.list().length);
//        try {
//            mappingService4Test.createMapping(content, mappingRecord);
//            fail();
//        } catch (IOException | MappingException ie) {
//            assertTrue(true);
//            assertTrue(ie.getMessage().contains("Unkown mapping"));
//            assertTrue(ie.getMessage().contains(mappingType));
//        }
//    }
//    @Test
//    public void testUpdateMapping() {
//        System.out.println("updateMapping");
//        String content = "content";
//        String newContent = "new content";
//        String mappingId = "mappingId";
//        String mappingType = "GEMMA_unknown";
//        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
//        MappingRecord mappingRecord = new MappingRecord();
//        mappingRecord.setMappingId(mappingId);
//        mappingRecord.setMappingType(mappingType);
//
////    MappingService instance = new MappingService(applicationProperties);
//        URL mappingsLocation = applicationProperties.getMappingsLocation();
//        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
//        assertEquals(0, mappingsDir.list().length);
//        try {
//            mappingService4Test.createMapping(content, mappingRecord);
//            assertEquals(1, mappingsDir.list().length);
//            assertEquals(expectedFilename, mappingsDir.list()[0]);
//            File mappingFile = Paths.get(mappingsDir.getAbsolutePath(), expectedFilename).toFile();
//            assertEquals(content, FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8));
//            mappingService4Test.updateMapping(newContent, mappingRecord);
//            assertEquals(2, mappingsDir.list().length);
//            for (String file : mappingsDir.list()) {
//                assertTrue(file.contains(expectedFilename));
//            }
//            assertTrue(mappingFile.exists());
//            assertEquals(newContent, FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8));
//
//        } catch (IOException | MappingException ie) {
//            assertTrue(true);
//            assertTrue(ie.getMessage().contains("missing mapping file"));
//        }
//    }
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

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();

//        assertEquals(0, mappingsDir.list().length);
        try {
            File mappingsDir = Paths.get(mappingsLocation.toURI()).toFile();
            mappingService4Test.updateMapping(content, mappingRecord);
            fail();
        } catch (IOException | MappingException | URISyntaxException ie) {
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
////    MappingService instance = new MappingService(applicationProperties);
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

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
//        assertEquals(0, mappingsDir.list().length);
        try {
            File mappingsDir = Paths.get(mappingsLocation.toURI()).toFile();
            mappingService4Test.deleteMapping(mappingRecord);
            fail();
        } catch (IOException | MappingException | URISyntaxException ie) {
//            assertEquals(0, mappingsDir.list().length);
            assertTrue(ie.getMessage().contains("Mapping"));
            assertTrue(ie.getMessage().contains("doesn't exist"));
            assertTrue(ie.getMessage().contains(mappingType));
        }
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithoutAnyParameter() {
        System.out.println("testExecuteMappingWithoutAnyParameter");
        URI contentUrl = null;
        String mappingId = "";
        try {
            Optional<Path> result = mappingService4Test.executeMapping(contentUrl, mappingId);
            fail("Exception expected!");
        } catch (MappingException | MappingPluginException ie) {
            assertTrue(ie.getMessage().contains("No URL provided"));
        }
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithWrongMappingId() throws IOException {
        System.out.println("testExecuteMappingWithWrongMappingId");
        URI contentUrl = null;
        String mappingId = "unknownMapping";
        try {
            Optional<Path> result = mappingService4Test.executeMapping(contentUrl, mappingId);
            fail("Exception expected!");
        } catch (MappingException | MappingPluginException ie) {
            assertTrue(ie.getMessage().contains("No URL provided"));

        }
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        contentUrl = srcFile.toURI();
        String expectedResult = FileUtils.readFileToString(srcFile, StandardCharsets.UTF_8);

        Optional<Path> resultPath = null;
        try {
            resultPath = mappingService4Test.executeMapping(contentUrl, mappingId);
        } catch (MappingPluginException e) {
            throw new RuntimeException(e);
        }

        assertTrue(resultPath.isPresent());
        assertTrue(resultPath.get().toFile().exists());
        String result = FileUtils.readFileToString(resultPath.get().toFile(), StandardCharsets.UTF_8);
        assertEquals(expectedResult, result);
        assertTrue(resultPath.get().toFile().delete());
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithWrongURI() throws URISyntaxException {
        System.out.println("testExecuteMappingWithWrongURI");
        String mappingId = "unknownMapping";
        String contentUrl = "https://unknown.site.which.doesnt.exist";
        try {
            URI url = new URI(contentUrl);
            Optional<Path> result = mappingService4Test.executeMapping(url, mappingId);
            fail("Exception expected!");
        } catch (MappingException | MappingPluginException ie) {
            assertTrue(ie.getMessage().contains("Error downloading resource"));
            assertTrue(ie.getMessage().contains(contentUrl));
        } catch (ResourceAccessException ex) {
            //happens under Windows...no idea why not under Unix
        }
    }

    @Test
    public void testExecuteMappingWithoutExistingMapping() throws IOException {
        System.out.println("executeMapping");
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        URI contentUrl = srcFile.toURI();
        String mappingId = "GEMMA_unknown";
        String expectedResult = FileUtils.readFileToString(srcFile, StandardCharsets.UTF_8);

        Optional<Path> resultPath = null;
        try {
            resultPath = mappingService4Test.executeMapping(contentUrl, mappingId);
        } catch (MappingPluginException e) {
            throw new RuntimeException(e);
        }

        assertTrue(resultPath.isPresent());
        assertTrue(resultPath.get().toFile().exists());
        String result = FileUtils.readFileToString(resultPath.get().toFile(), StandardCharsets.UTF_8);
        assertEquals(expectedResult, result);
        assertTrue(resultPath.get().toFile().delete());
    }

//    /**
//     * Test of executeMapping method, of class MappingService.
//     */
//    @Test
//    public void testExecuteMapping() throws IOException {
//        System.out.println("executeMapping");
//        MappingRecord mappingRecord = new MappingRecord();
//        String mappingId = "myMappingId";
//        String mappingType = "GEMMA_unknown";
//        String mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsolutePath();
//        mappingRecord.setMappingId(mappingId);
//        mappingRecord.setMappingDocumentUri(mappingFile);
//        mappingRecord.setMappingType(mappingType);
//        mappingRepo.save(mappingRecord);
//        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
//        assertTrue(srcFile.exists());
//        URI contentUrl = srcFile.toURI();
//        String expectedResult = FileUtils.readFileToString(new File("src/test/resources/result/gemma/simple.elastic.json"), StandardCharsets.UTF_8);
//        Optional<Path> resultPath = null;
//        try {
//            resultPath = mappingService4Test.executeMapping(contentUrl, mappingId);
//        } catch (MappingPluginException e) {
//            throw new RuntimeException(e);
//        }
//        assertTrue(resultPath.isPresent());
//        assertTrue(resultPath.get().toFile().exists());
//        String result = FileUtils.readFileToString(resultPath.get().toFile(), StandardCharsets.UTF_8);
//        assertEquals(expectedResult, result);
//        assertTrue(resultPath.get().toFile().delete());
//    }
//    @Test
//    public void testExecuteMappingWithoutgivenMapping() throws IOException {
//        System.out.println("executeMapping");
//        MappingRecord mappingRecord = new MappingRecord();
//        String mappingId = "myMappingId";
//        String mappingType = "GEMMA_unknown";
//        String mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsolutePath();
//        mappingRecord.setMappingId(mappingId);
//        mappingRecord.setMappingDocumentUri(mappingFile);
//        mappingRecord.setMappingType(mappingType);
//        mappingRepo.save(mappingRecord);
//        mappingRecord.setMappingType("unknownMapping");
//        mappingRepo.save(mappingRecord);
//        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
//        URI contentUrl = srcFile.toURI();
//        String expectedResult = FileUtils.readFileToString(new File("src/test/resources/result/gemma/simple.elastic.json"), StandardCharsets.UTF_8);
//        List<Path> resultPath = null;
//        try {
//            resultPath = Collections.singletonList(mappingService4Test.executeMapping(contentUrl, mappingId).get());
//        } catch (MappingPluginException e) {
//            throw new RuntimeException(e);
//        }
//        assertFalse(resultPath.isEmpty());
//        assertEquals(1, resultPath.size());
//        assertTrue(resultPath.get(0).toFile().exists());
//        String result = FileUtils.readFileToString(resultPath.get(0).toFile(), StandardCharsets.UTF_8);
//        assertEquals(expectedResult, result);
//        assertTrue(resultPath.get(0).toFile().delete());
//    }
}
