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
package edu.kit.datamanager.indexer.util;

import edu.kit.datamanager.indexer.exception.IndexerException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hartmann-v
 */
public class IndexerUtilTest {
  
  public IndexerUtilTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
  }
  
  @After
  public void tearDown() {
  }

 
  /**
   * Test of downloadResource method, of class GemmaMapping.
   */
  @Test
  public void testDownloadResource() throws URISyntaxException {
    System.out.println("downloadResource");
    assertNotNull(new IndexerUtil());
    URI resourceURL = new URI("https://www.example.org");
     Optional<Path> result = IndexerUtil.downloadResource(resourceURL);
    assertTrue(result.isPresent());
    assertTrue(result.get().toFile().exists());
    assertTrue(result.get().toFile().delete());

    resourceURL = new URI("https://invalidhttpaddress.de");
    result = IndexerUtil.downloadResource(resourceURL);
    assertTrue(!result.isPresent());
  }

  /**
   * Test of createTempFile method, of class IndexerUtil.
   */
  @Test
  public void testCreateTempFile() {
    System.out.println("createTempFile");
    String[] prefix = {null, null, null,     "",   "", "",       "prefix", "prefix", "prefix"};
    String[] suffix = {null, "",   "suffix", null, "", "suffix", null,     "",       "suffix"};
    HashSet<String> allPaths = new HashSet<>();
    String path = null;
    for (int index = 0; index < prefix.length; index++) {
      Path tmpPath = IndexerUtil.createTempFile(prefix[index], suffix[index]);
      String tmpFile = tmpPath.getFileName().toString();
      path = tmpPath.getParent().toString();
      assertFalse(allPaths.contains(tmpFile));
      allPaths.add(tmpFile);
      if ((prefix[index] != null) && (!prefix[index].trim().isEmpty())) {
        assertTrue(tmpFile.startsWith(prefix[index]));
      } else {
        assertTrue(tmpFile.startsWith(IndexerUtil.DEFAULT_PREFIX));
      }
      if ((suffix[index] != null) && (!suffix[index].trim().isEmpty())) {
        assertTrue(tmpFile.endsWith(suffix[index]));
      } else {
        assertTrue(tmpFile.endsWith(IndexerUtil.DEFAULT_SUFFIX));
      }
    }
     for (String filename : allPaths) {
       IndexerUtil.removeFile(Paths.get(path, filename));
     }
  }

  /**
   * Test of removeFile method, of class IndexerUtil.
   */
  @Test
  public void testRemoveFile() {
    System.out.println("removeFile");
    Path createTempFile = IndexerUtil.createTempFile("testRemoveDir", ".txt");
    try {
    IndexerUtil.removeFile(createTempFile.getParent());
    assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(ie.getMessage().contains("Error removing file"));
    }
    assertTrue(createTempFile.toFile().exists());
    IndexerUtil.removeFile(createTempFile);
    assertFalse(createTempFile.toFile().exists());
  }
  
}
