/*
 * Copyright 2020 hartmann-v.
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
package edu.kit.datamanager.mappingservice.util;

import com.google.common.io.Files;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author hartmann-v
 */
public class FileUtilTest {

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadResource() throws URISyntaxException {
        System.out.println("downloadResource");
        assertNotNull(new FileUtil());
        URI resourceURL = new URI("https://www.example.org");
        Optional<Path> result = FileUtil.downloadResource(resourceURL);
        assertTrue(result.isPresent());
        assertTrue(result.get().toFile().exists());
        assertTrue(result.get().toString().endsWith("html"));
        assertTrue(result.get().toFile().delete());
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadResourceWithPath() throws URISyntaxException {
        System.out.println("downloadResource");
        assertNotNull(new FileUtil());
        URI resourceURL = new URI("https://www.example.org/index.html");
        Optional<Path> result = FileUtil.downloadResource(resourceURL);
        assertTrue(result.isPresent());
        assertTrue(result.get().toFile().exists());
        assertTrue(result.get().toString().endsWith(".html"));
        assertTrue(result.get().toFile().delete());
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadInvalidResource() throws URISyntaxException {
        System.out.println("testDownloadInvalidResource");

        try {
            URI resourceURL = new URI("https://invalidhttpaddress.de");
            Optional<Path> result = FileUtil.downloadResource(resourceURL);
            assertTrue(false);
        } catch (MappingException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("Error downloading resource"));
        }
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadLocalResource() {
        System.out.println("testDownloadLocalResource");
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        assertTrue(srcFile.exists());
        URI resourceURL = srcFile.toURI();
        Optional<Path> result = FileUtil.downloadResource(resourceURL);
        assertTrue(result.isPresent());
        assertTrue(result.get().toFile().exists());
        assertTrue(result.get().toString().endsWith(".json"));
        assertTrue(result.get().toFile().delete());
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadLocalJsonFileWithoutSuffix() throws IOException {
        System.out.println("testDownloadLocalResource");
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        assertTrue(srcFile.exists());
        Path createTempFile = FileUtil.createTempFile(null, "nosuffix");
        Files.copy(srcFile, createTempFile.toFile());
        Optional<Path> result = FileUtil.downloadResource(createTempFile.toUri());
        assertTrue(result.isPresent());
        assertTrue(result.get().toFile().exists());
        assertTrue(result.get().toString().endsWith(".json"));
        assertTrue(result.get().toFile().delete());
        assertTrue(createTempFile.toFile().delete());
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadLocalXMLFileWithoutSuffix() throws IOException {
        System.out.println("testDownloadLocalResource");
        File srcFile = new File("src/test/resources/examples/gemma/simple.xml");
        assertTrue(srcFile.exists());
        Path createTempFile = FileUtil.createTempFile(null, "nosuffix");
        Files.copy(srcFile, createTempFile.toFile());
        Optional<Path> result = FileUtil.downloadResource(createTempFile.toUri());
        assertTrue(result.isPresent());
        assertTrue(result.get().toFile().exists());
        assertTrue(result.get().toString().endsWith(".xml"));
        assertTrue(result.get().toFile().delete());
        assertTrue(createTempFile.toFile().delete());
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadLocalResourceWithoutSuffix() {
        System.out.println("testDownloadLocalResource");
        File srcFile = new File("src/test/resources/examples/anyContentWithoutSuffix");
        String expectedExtension = ".txt";
        assertTrue(srcFile.exists());
        Optional<Path> result = FileUtil.downloadResource(srcFile.getAbsoluteFile().toURI());
        assertTrue(result.isPresent());
        assertTrue(result.get().toFile().exists());
        assertTrue(result.get().toString().endsWith(expectedExtension));
        assertTrue(result.get().toFile().delete());
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadInvalidLocalResource() {
        System.out.println("testDownloadInvalidLocalResource");
        try {
            URI resourceURL = new File("/invalid/path/to/local/file").toURI();
            Optional<Path> result = FileUtil.downloadResource(resourceURL);
            assertTrue(false);
        } catch (MappingException ie) {
            assertTrue(true);
            assertTrue(ie.getMessage().contains("Error downloading resource"));
        }
    }

    /**
     * Test of downloadResource method, of class GemmaMapping.
     */
    @Test
    public void testDownloadResourceNoParameter() {
        System.out.println("downloadResource");
        Optional<Path> result = FileUtil.downloadResource(null);
        assertFalse(result.isPresent());
    }

    /**
     * Test of createTempFile method, of class FileUtil.
     */
    @Test
    public void testCreateTempFile() {
        System.out.println("createTempFile");
        String[] prefix = {null, null, null, "", "", "", "prefix", "prefix", "prefix"};
        String[] suffix = {null, "", "suffix", null, "", "suffix", null, "", "suffix"};
        HashSet<String> allPaths = new HashSet<>();
        String path = null;
        for (int index = 0; index < prefix.length; index++) {
            Path tmpPath = FileUtil.createTempFile(prefix[index], suffix[index]);
            String tmpFile = tmpPath.getFileName().toString();
            path = tmpPath.getParent().toString();
            assertFalse(allPaths.contains(tmpFile));
            allPaths.add(tmpFile);
            if ((prefix[index] != null) && (!prefix[index].trim().isEmpty())) {
                assertTrue(tmpFile.startsWith(prefix[index]));
            } else {
                assertTrue(tmpFile.startsWith(FileUtil.DEFAULT_PREFIX));
            }
            if ((suffix[index] != null) && (!suffix[index].trim().isEmpty())) {
                assertTrue(tmpFile.endsWith(suffix[index]));
            } else {
                assertTrue(tmpFile.endsWith(FileUtil.DEFAULT_SUFFIX));
            }
        }
        for (String filename : allPaths) {
            FileUtil.removeFile(Paths.get(path, filename));
        }
    }

    /**
     * Test of removeFile method, of class FileUtil.
     */
    @Test
    public void testRemoveFile() {
        System.out.println("removeFile");
        Path createTempFile = FileUtil.createTempFile("testRemoveDir", ".txt");
        try {
            FileUtil.removeFile(createTempFile.getParent());
            assertTrue(false);
        } catch (MappingException ie) {
            assertTrue(ie.getMessage().contains("Error removing file"));
        }
        assertTrue(createTempFile.toFile().exists());
        FileUtil.removeFile(createTempFile);
        assertFalse(createTempFile.toFile().exists());
    }

    /**
     * Test of fixFileExtension method, of class FileUtil.
     */
    @Test
    public void testFixFileExtensionXml() throws IOException {
        System.out.println("testFixFileExtensionXml");
        File srcFile = new File("src/test/resources/examples/gemma/simple.xml");
        assertTrue(srcFile.exists());
        String[] extensions = {"nosuffix", "xml", ".xml", ".xsd", ".json"};
        for (String extension : extensions) {
            Path createTempFile = FileUtil.createTempFile(null, extension);
            Files.copy(srcFile, createTempFile.toFile());
            Path result = FileUtil.fixFileExtension(createTempFile);
            assertTrue(result.toString().endsWith(".xml"));
            assertTrue(result.toFile().delete());
        }
    }

    @Test
    public void testFixFileExtensionJson() throws IOException {
        System.out.println("testFixFileExtensionJson");
        File srcFile = new File("src/test/resources/examples/gemma/simple.json");
        assertTrue(srcFile.exists());
        String[] extensions = {"nosuffix", "json", ".json", ".xml"};
        for (String extension : extensions) {
            Path createTempFile = FileUtil.createTempFile(null, extension);
            Files.copy(srcFile, createTempFile.toFile());
            Path result = FileUtil.fixFileExtension(createTempFile);
            assertTrue(result.toString().endsWith(".json"));
            assertTrue(result.toFile().delete());

        }
    }

    @Test
    public void testFixFileExtensionUnknown() throws IOException {
        System.out.println("testFixFileExtensionUnknown");

        File srcFile = new File("src/test/resources/examples/anyContentWithoutSuffix");
        assertTrue(srcFile.exists());
        String[] extensions = {"", ".json", ".xml", ".txt"};
        String expectedExtension = ".txt";
        for (String extension : extensions) {
            Path createTempFile = FileUtil.createTempFile(null, extension);
            Files.copy(srcFile, createTempFile.toFile());
            Path result = FileUtil.fixFileExtension(createTempFile);
            if (extension.startsWith(".")) {
                assertTrue(result.toString().endsWith(extension), "Result: " + result.toString());
            } else {
                assertTrue(result.toString().endsWith(expectedExtension), "Result: " + result.toString());
            }
            assertTrue(result.toFile().delete());

        }
    }

    @Test
    public void testFixZipFileExtensionUnknown() throws IOException {
        System.out.println("testFixFileExtensionUnknown");

        File srcFile = new File("src/test/resources/examples/record_json_zip");
        assertTrue(srcFile.exists());
        String[] extensions = {"nosuffix", ".zip", ".json", ".xml"};
        String expectedExtension = ".zip";
        for (String extension : extensions) {
            Path createTempFile = FileUtil.createTempFile(null, extension);
            Files.copy(srcFile, createTempFile.toFile());
            Path result = FileUtil.fixFileExtension(createTempFile);
            assertTrue(result.toString().endsWith(expectedExtension));
            assertTrue(result.toFile().delete());

        }
    }

    @Test
    public void testFixFileExtensionWrongFile() {
        System.out.println("testFixFileExtensionUnknown");
        File srcFile = new File("/tmp");
        Path result = FileUtil.fixFileExtension(srcFile.toPath());
        assertEquals(result, srcFile.toPath());
        srcFile = new File("/invalid/path/for/file");
        result = FileUtil.fixFileExtension(srcFile.toPath());
        assertEquals(result, srcFile.toPath());
        srcFile = null;
        result = FileUtil.fixFileExtension(null);
        assertNull(result);
    }

    @Test
    void cloneValidGitRepository() {
        Path util = null;
        try {
            util = FileUtil.cloneGitRepository("https://github.com/maximilianiKIT/mapping-service.git", "main", "/tmp/test");
        } catch (Exception e) {
            fail(e);
        }
        try {
            FileUtils.deleteDirectory(new File("tmp/test"));
        } catch (IOException e) {
        }
        assertNotNull(util);
        util = null;
        try {
            util = FileUtil.cloneGitRepository("https://github.com/maximilianiKIT/mapping-service.git", "main");
        } catch (Exception e) {
            fail(e);
        }
        assertNotNull(util);
        try {
            FileUtils.deleteDirectory(new File(util.toUri()));
        } catch (IOException e) {
        }
        util = null;
    }

    @Test
    void cloneInvalidGitRepository() {
        assertThrows(MappingException.class, () -> FileUtil.cloneGitRepository("test", "test", "test"));
        assertThrows(MappingException.class, () -> FileUtil.cloneGitRepository("test", "test"));
    }

    @AfterEach
    void tearDown() {
        try {
            FileUtils.deleteDirectory(new File("lib"));
        } catch (IOException ignored) {
        }
    }
}
