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
package edu.kit.datamanager.mappingservice.indexer.service.impl;

import edu.kit.datamanager.mappingservice.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.indexer.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.indexer.exception.IndexerException;
import edu.kit.datamanager.mappingservice.indexer.mapping.Mapping;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static edu.kit.datamanager.mappingservice.indexer.mapping.Mapping.GEMMA;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
//@RunWith(SpringRunner.class)
//@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
//@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
//@PrepareForTest(AuthenticationHelper.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class
})
@ActiveProfiles("test")
public class MappingServiceTest {

    @Autowired
    ApplicationProperties applicationProperties;

    @Autowired
    IMappingRecordDao mappingRepo;

    @Autowired
    MappingService mappingService4Test;

    private final static String TEMP_DIR_4_ALL = "/tmp/metastore2/";
    private final static String TEMP_DIR_4_MAPPING = TEMP_DIR_4_ALL + "mapping/";


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
    public void testConstructorRelativePath() throws IOException {
        try {
            URL relativePath = new URL("file:tmp/relativePath");
            ApplicationProperties ap = new ApplicationProperties();
            ap.setMappingsLocation(relativePath);
            File file = new File(relativePath.getPath());
            assertFalse(file.exists());
            assertTrue(file.exists());
            FileUtils.deleteDirectory(file);
            assertFalse(file.exists());
        } catch (IndexerException ie) {
            fail();
        }
    }

    @Test
    public void testConstructorFailing() throws IOException, URISyntaxException {
        try {
            new MappingService(null);
            fail();
        } catch (IndexerException ie) {
            assertTrue(true);
        }
        try {
            ApplicationProperties ap = new ApplicationProperties();
            ap.setMappingsLocation(new URL("file:///forbidden"));
            new MappingService(ap);
            fail();
        } catch (IndexerException ie) {
            assertTrue(true);
        }
    }

    /**
     * Test of createMapping method, of class MappingService.
     */
    @Test
    public void testCreateMapping() {
        System.out.println("createMapping");
        String content = "content";
        String mappingId = "mappingId";
        String mappingType = GEMMA.name();
        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(0, mappingsDir.list().length);
        Paths.get(applicationProperties.getMappingsLocation().getPath(), mappingId);
        try {
            mappingService4Test.createMapping(content, mappingRecord);
            assertEquals(1, mappingsDir.list().length);
            assertEquals(expectedFilename, mappingsDir.list()[0]);
            File mappingFile = Paths.get(mappingsDir.getAbsolutePath(), expectedFilename).toFile();
            assertEquals(content, FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8));
        } catch (IOException | IndexerException ie) {
            fail();
        }
    }

    /**
     * Test of createMapping method, of class MappingService.
     */
    @Test
    public void testCreateMappingTwice() {
        System.out.println("createMapping");
        String content = "content";
        String newContent = "newContent";
        String mappingId = "mappingId";
        String mappingType = GEMMA.name();
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(0, mappingsDir.list().length);
        try {
            mappingService4Test.createMapping(content, mappingRecord);
            mappingService4Test.createMapping(newContent, mappingRecord);
            fail();
        } catch (IOException | IndexerException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("already exists"));
            assertTrue(ie.getMessage().contains(mappingType));
        }
    }

    /**
     * Test of createMapping method, of class MappingService.
     */
    @Test
    public void testCreateMappingWithWrongMapping() {
        System.out.println("createMapping");
        String content = "content";
        String mappingId = "mappingId";
        String mappingType = "aTotallyUnknownMapping";
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(0, mappingsDir.list().length);
        try {
            mappingService4Test.createMapping(content, mappingRecord);
            fail();
        } catch (IOException | IndexerException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("Unkown mapping"));
            assertTrue(ie.getMessage().contains(mappingType));
        }
    }

    @Test
    public void testUpdateMapping() {
        System.out.println("updateMapping");
        String content = "content";
        String newContent = "new content";
        String mappingId = "mappingId";
        String mappingType = GEMMA.name();
        String expectedFilename = mappingId + "_" + mappingType + ".mapping";
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(0, mappingsDir.list().length);
        try {
            mappingService4Test.createMapping(content, mappingRecord);
            assertEquals(1, mappingsDir.list().length);
            assertEquals(expectedFilename, mappingsDir.list()[0]);
            File mappingFile = Paths.get(mappingsDir.getAbsolutePath(), expectedFilename).toFile();
            assertEquals(content, FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8));
            mappingService4Test.updateMapping(newContent, mappingRecord);
            assertEquals(2, mappingsDir.list().length);
            for (String file : mappingsDir.list()) {
                assertTrue(file.contains(expectedFilename));
            }
            assertTrue(mappingFile.exists());
            assertEquals(newContent, FileUtils.readFileToString(mappingFile, StandardCharsets.UTF_8));

        } catch (IOException | IndexerException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("missing mapping file"));
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
        String mappingType = GEMMA.name();
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(0, mappingsDir.list().length);
        Paths.get(applicationProperties.getMappingsLocation().getPath(), mappingId);
        try {
            mappingService4Test.updateMapping(content, mappingRecord);
            fail();
        } catch (IOException | IndexerException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("Mapping"));
            assertTrue(ie.getMessage().contains("doesn't exist"));
            assertTrue(ie.getMessage().contains(mappingType));
        }
    }

    /**
     * Test of deleteMapping method, of class MappingService.
     */
    @Test
    public void testDeleteMapping() {
        System.out.println("deleteMapping");
        String content = "content";
        assertEquals(0, mappingRepo.count());
        testCreateMapping();
        assertEquals(1, mappingRepo.count());
        MappingRecord mappingRecord = mappingRepo.findAll().get(0);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(1, mappingsDir.list().length);
        File mappingFile = new File(mappingRecord.getMappingDocumentUri());
        try {
            assertTrue(mappingFile.exists());
            assertEquals(mappingFile.getName(), mappingsDir.list()[0]);

            mappingService4Test.deleteMapping(mappingRecord);
            assertEquals(1, mappingsDir.list().length);
            assertFalse(mappingFile.exists());

            File mappingFileMarkedAsDeleted = Paths.get(mappingsDir.getAbsolutePath(), mappingsDir.list()[0]).toFile();
            assertTrue(mappingFileMarkedAsDeleted.getName().contains(mappingFile.getName()));
            assertTrue(mappingFileMarkedAsDeleted.exists());
            assertEquals(content, FileUtils.readFileToString(mappingFileMarkedAsDeleted, StandardCharsets.UTF_8));
            assertEquals(0, mappingRepo.count());
        } catch (IOException | IndexerException ie) {
            fail();
        }
    }

    @Test
    public void testDeleteNotExistingMapping() {
        System.out.println("createMapping");
        String mappingId = "mappingId";
        String mappingType = GEMMA.name();
        MappingRecord mappingRecord = new MappingRecord();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingType(mappingType);

//    MappingService instance = new MappingService(applicationProperties);
        URL mappingsLocation = applicationProperties.getMappingsLocation();
        File mappingsDir = Paths.get(mappingsLocation.getPath()).toFile();
        assertEquals(0, mappingsDir.list().length);
        try {
            mappingService4Test.deleteMapping(mappingRecord);
            fail();
        } catch (IOException | IndexerException ie) {
            assertEquals(0, mappingsDir.list().length);
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
        String mappingId = "";
        try {
            mappingService4Test.executeMapping(null, mappingId, GEMMA.name());
            fail("Exception expected!");
        } catch (IndexerException ie) {
            assertTrue(ie.getMessage().contains("No URL provided"));
        }
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithWrongMappingId() throws IOException {
        System.out.println("testExecuteMappingWithWrongMappingId");
        URI contentUrl;
        String mappingId = "unknownMapping";
        try {
            mappingService4Test.executeMapping(null, mappingId, GEMMA.name());
            fail("Exception expected!");
        } catch (IndexerException ie) {
            assertTrue(ie.getMessage().contains("No URL provided"));

        }
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        contentUrl = srcFile.toURI();
        String expectedResult = FileUtils.readFileToString(srcFile, StandardCharsets.UTF_8);

        Optional<Path> resultPath = mappingService4Test.executeMapping(contentUrl, mappingId, GEMMA.name());

        assertTrue(resultPath.isPresent());
        assertTrue(resultPath.get().toFile().exists());
        String result = FileUtils.readFileToString(resultPath.get().toFile(), StandardCharsets.UTF_8);
        assertEquals("Expected result: ", expectedResult, result);
        assertTrue(resultPath.get().toFile().delete());
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMappingWithWrongURI() throws URISyntaxException {
        System.out.println("testExecuteMappingWithWrongURI");
        URI contentUrl = new URI("https://unknown.site.which.doesnt.exist");
        String mappingId = "unknownMapping";
        try {
            mappingService4Test.executeMapping(contentUrl, mappingId, GEMMA.name());
            fail("Exception expected!");
        } catch (IndexerException ie) {
            assertTrue(ie.getMessage().contains("Error downloading resource"));
            assertTrue(ie.getMessage().contains(contentUrl.toString()));
        }
    }

    @Test
    public void testExecuteMappingWithoutExistingMapping() throws IOException {
        System.out.println("executeMapping");
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        URI contentUrl = srcFile.toURI();
        String mappingId = Mapping.GEMMA.name();
        String expectedResult = FileUtils.readFileToString(srcFile, StandardCharsets.UTF_8);

        Optional<Path> resultPath = mappingService4Test.executeMapping(contentUrl, mappingId, GEMMA.name());

        assertTrue(resultPath.isPresent());
        assertTrue(resultPath.get().toFile().exists());
        String result = FileUtils.readFileToString(resultPath.get().toFile(), StandardCharsets.UTF_8);
        assertEquals("Expected result: ", expectedResult, result);
        assertTrue(resultPath.get().toFile().delete());
    }

    /**
     * Test of executeMapping method, of class MappingService.
     */
    @Test
    public void testExecuteMapping() throws IOException {
        System.out.println("executeMapping");
        MappingRecord mappingRecord = new MappingRecord();
        String mappingId = "myMappingId";
        String mappingType = GEMMA.name();
        String mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsolutePath();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingDocumentUri(mappingFile);
        mappingRecord.setMappingType(mappingType);
        mappingRepo.save(mappingRecord);
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        assertTrue(srcFile.exists());
        URI contentUrl = srcFile.toURI();
        String expectedResult = FileUtils.readFileToString(new File("src/test/resources/result/gemma/simple.elastic.json"), StandardCharsets.UTF_8);
        Optional<Path> resultPath = mappingService4Test.executeMapping(contentUrl, mappingId, GEMMA.name());
        assertTrue(resultPath.isPresent());
        assertTrue(resultPath.get().toFile().exists());
        String result = FileUtils.readFileToString(resultPath.get().toFile(), StandardCharsets.UTF_8);
        assertEquals("Expected result: ", expectedResult, result);
        assertTrue(resultPath.get().toFile().delete());
    }

    @Test
    public void testExecuteMappingWithoutgivenMapping() throws IOException {
        System.out.println("executeMapping");
        MappingRecord mappingRecord = new MappingRecord();
        String mappingId = "myMappingId";
        String mappingType = GEMMA.name();
        String mappingFile = new File("src/test/resources/mapping/gemma/simple.mapping").getAbsolutePath();
        mappingRecord.setMappingId(mappingId);
        mappingRecord.setMappingDocumentUri(mappingFile);
        mappingRecord.setMappingType(mappingType);
        mappingRepo.save(mappingRecord);
        mappingRecord.setMappingType("unknownMapping");
        mappingRepo.save(mappingRecord);
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        URI contentUrl = srcFile.toURI();
        String expectedResult = FileUtils.readFileToString(new File("src/test/resources/result/gemma/simple.elastic.json"), StandardCharsets.UTF_8);
        List<Path> resultPath = mappingService4Test.executeMapping(contentUrl, mappingId);
        assertFalse(resultPath.isEmpty());
        assertEquals(1, resultPath.size());
        assertTrue(resultPath.get(0).toFile().exists());
        String result = FileUtils.readFileToString(resultPath.get(0).toFile(), StandardCharsets.UTF_8);
        assertEquals("Expected result: ", expectedResult, result);
        assertTrue(resultPath.get(0).toFile().delete());
    }
}