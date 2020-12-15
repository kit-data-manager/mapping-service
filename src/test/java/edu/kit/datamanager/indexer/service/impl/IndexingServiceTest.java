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
package edu.kit.datamanager.indexer.service.impl;

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 */
public class IndexingServiceTest {

  public IndexingServiceTest() {
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
   * Test of getFromElastic method, of class IndexingService.
   */
  @Test
  public void testConstructor() throws MalformedURLException {
    System.out.println("getFromElastic");
    ApplicationProperties ap = null;
    try {
      new IndexingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    ap = new ApplicationProperties();
    try {
      new IndexingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    ap.setElasticsearchUrl(new URL("http://localhost:9201"));
    try {
      new IndexingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    ap.setElasticsearchUrl(new URL("http://localhost:9200/"));
    try {
      new IndexingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    ap.setElasticsearchUrl(new URL("http://localhost:9200/"));
    try {
      new IndexingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    ap.setElasticsearchUrl(new URL("http://localhost:9200/kitdm"));
    try {
      new IndexingService(ap);
      assertTrue(false);
    } catch (IndexerException ie) {
      assertTrue(true);
    }
    // Test with correct values
    ap.setElasticsearchUrl(new URL("http://localhost:9200"));
    try {
      assertNotNull(new IndexingService(ap));
      assertTrue(true);
    } catch (IndexerException ie) {
      assertTrue(false);
    }
  }

  /**
   * Test of getFromElastic method, of class IndexingService.
   */
  @Test
  public void testGetFromElasticSimpleId() throws MalformedURLException {
    System.out.println("getFromElastic");
    String jsonDocument = "{\"name\":\"volker\"}";
    String index = "junittest";
    String documentId = "idwithoutspaces";
    ApplicationProperties ap = new ApplicationProperties();
    ap.setElasticsearchUrl(new URL("http://localhost:9200"));
    IndexingService is = new IndexingService(ap);
    is.uploadToElastic(jsonDocument, index, documentId);
    ResponseEntity<String> fromElastic = is.getFromElastic(index, documentId);
    assertEquals("HTTP status is not 200!", fromElastic.getStatusCodeValue(), HttpStatus.OK.value());
    String result = is.getDocumentFromResponse(fromElastic);
    assertEquals("Index document is not identical to json document!", jsonDocument, result);
  }

  @Test
  public void testGetFromElastic() throws MalformedURLException {
    System.out.println("getFromElastic");
    String jsonDocument = "{\"name\":\"volker\"}";
    String index = "junittest";
    String documentId = "id with spaces";
    ApplicationProperties ap = new ApplicationProperties();
    ap.setElasticsearchUrl(new URL("http://localhost:9200"));
    IndexingService is = new IndexingService(ap);
    is.uploadToElastic(jsonDocument, index, documentId);
    ResponseEntity<String> fromElastic = is.getFromElastic(index, documentId);
    assertEquals("HTTP status is not 200!", fromElastic.getStatusCodeValue(), HttpStatus.OK.value());
    String result = is.getDocumentFromResponse(fromElastic);
    assertEquals("Index document is not identical to json document!", jsonDocument, result);
  }

}
