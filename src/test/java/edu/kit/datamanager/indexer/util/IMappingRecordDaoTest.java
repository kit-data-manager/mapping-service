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

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.domain.MappingRecord;
import edu.kit.datamanager.indexer.service.impl.MappingService;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {
  DependencyInjectionTestExecutionListener.class,
  TransactionalTestExecutionListener.class
})
@ActiveProfiles("test")
public class IMappingRecordDaoTest {

  @Autowired
  ApplicationProperties applicationProperties;

  @Autowired
  IMappingRecordDao mappingRepo;

  @Autowired
  MappingService mappingService4Test;

  public IMappingRecordDaoTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
    mappingRepo.deleteAll();
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testRepo() {
    assertEquals("Number of datasets: ", 0, mappingRepo.count());
    String id = "anyId";
    String mappingType = "anyMappingType";
    String uri = "anyURI";
    String id_2 = "anotherId";
    String mappingType_2 = "anotherMappingType";
    MappingRecord mappingRecord = new MappingRecord();
    try {
      mappingRepo.save(mappingRecord);
      assertTrue("Exception expected!", false);
    } catch (DataIntegrityViolationException dive) {
      assertTrue(true);
    }
    assertEquals("Number of datasets: ", 0, mappingRepo.count());
    mappingRecord.setMappingId(id);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue("Exception expected!", false);
    } catch (DataIntegrityViolationException dive) {
      assertTrue(true);
    }
    assertEquals("Number of datasets: ", 0, mappingRepo.count());
    mappingRecord.setMappingType(mappingType);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 1, mappingRepo.count());
    mappingRecord.setMappingDocumentUri(uri);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 1, mappingRepo.count());

    assertTrue(mappingRepo.findByMappingIdAndMappingType(id, mappingType).isPresent());
    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, mappingType_2).isPresent());
    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id_2, mappingType).isPresent());
    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id_2, mappingType_2).isPresent());

    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, null).isPresent());

    mappingRecord.setMappingId(id_2);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 2, mappingRepo.count());

    assertTrue(mappingRepo.findByMappingIdAndMappingType(id, mappingType).isPresent());
    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, mappingType_2).isPresent());
    assertTrue(mappingRepo.findByMappingIdAndMappingType(id_2, mappingType).isPresent());
    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id_2, mappingType_2).isPresent());

    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, null).isPresent());
    mappingRecord.setMappingType(mappingType_2);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 3, mappingRepo.count());

    assertTrue(mappingRepo.findByMappingIdAndMappingType(id, mappingType).isPresent());
    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, mappingType_2).isPresent());
    assertTrue(mappingRepo.findByMappingIdAndMappingType(id_2, mappingType).isPresent());
    assertTrue(mappingRepo.findByMappingIdAndMappingType(id_2, mappingType_2).isPresent());

    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, null).isPresent());
    mappingRecord.setMappingId(id);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 4, mappingRepo.count());

    assertTrue(mappingRepo.findByMappingIdAndMappingType(id, mappingType).isPresent());
    assertTrue(mappingRepo.findByMappingIdAndMappingType(id, mappingType_2).isPresent());
    assertTrue(mappingRepo.findByMappingIdAndMappingType(id_2, mappingType).isPresent());
    assertTrue(mappingRepo.findByMappingIdAndMappingType(id_2, mappingType_2).isPresent());

    assertTrue(!mappingRepo.findByMappingIdAndMappingType(id, null).isPresent());
  }

  @Test
  public void testFindByMappingId() {
    assertEquals("Number of datasets: ", 0, mappingRepo.count());
    String id = "anyId";
    String mappingType = "anyMappingType";
    String uri = "anyURI";
    String id_2 = "anotherId";
    String mappingType_2 = "anotherMappingType";
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setMappingId(id);
    mappingRecord.setMappingType(mappingType);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 1, mappingRepo.count());
    mappingRecord.setMappingDocumentUri(uri);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 1, mappingRepo.count());

    assertTrue(mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id), Arrays.asList((String) null)).iterator().hasNext());
    assertTrue(!mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id_2), Arrays.asList((String) null)).iterator().hasNext());

    mappingRecord.setMappingId(id_2);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 2, mappingRepo.count());

    assertTrue(mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id), Arrays.asList((String) null)).iterator().hasNext());
    assertTrue(mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id_2), Arrays.asList((String) null)).iterator().hasNext());

    mappingRecord.setMappingType(mappingType_2);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 3, mappingRepo.count());

    Iterator iterator4id = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id), Arrays.asList((String) null)).iterator();
    Iterator iterator4id_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id_2), Arrays.asList((String) null)).iterator();
    assertTrue(iterator4id.hasNext());
    iterator4id.next();
    assertTrue(!iterator4id.hasNext());
    assertTrue(iterator4id_2.hasNext());
    iterator4id_2.next();
    assertTrue(iterator4id_2.hasNext());

    mappingRecord.setMappingId(id);
    try {
      mappingRepo.save(mappingRecord);
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }
    assertEquals("Number of datasets: ", 4, mappingRepo.count());
    // found 2 results for each mapping id
    iterator4id = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id), Arrays.asList((String) null)).iterator();
    iterator4id_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(id_2), Arrays.asList((String) null)).iterator();
    assertTrue(iterator4id.hasNext());
    iterator4id.next();
    assertTrue(iterator4id.hasNext());
    assertTrue(iterator4id_2.hasNext());
    iterator4id_2.next();
    assertTrue(iterator4id_2.hasNext());
    // Found 2 results for each mapping type
    iterator4id = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList((String) null), Arrays.asList((String) mappingType)).iterator();
    iterator4id_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList((String) null), Arrays.asList((String) mappingType_2)).iterator();
    assertTrue(iterator4id.hasNext());
    iterator4id.next();
    assertTrue(iterator4id.hasNext());
    assertTrue(iterator4id_2.hasNext());
    iterator4id_2.next();
    assertTrue(iterator4id_2.hasNext());
    // No value if all arguments are null
    iterator4id = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList((String) null), Arrays.asList((String) null)).iterator();
    assertTrue(!iterator4id.hasNext());
  }

  @Test
  public void testFindByMappingIdWithPage() {
    assertEquals("Number of datasets: ", 0, mappingRepo.count());
    String mappingId = "anyId";
    String mappingType = "anyMappingType";
    String uri = "anyURI";
    String mappingId_2 = "anotherId";
    String mappingType_2 = "anotherMappingType";
    MappingRecord mappingRecord = new MappingRecord();
    mappingRecord.setMappingId(mappingId);
    mappingRecord.setMappingType(mappingType);
    mappingRecord.setMappingDocumentUri(uri);
    try {
      mappingRepo.save(mappingRecord);
      mappingRecord.setMappingId(mappingId_2);

      mappingRepo.save(mappingRecord);
      mappingRecord.setMappingType(mappingType_2);
      mappingRepo.save(mappingRecord);

      mappingRecord.setMappingId(mappingId);
      mappingRepo.save(mappingRecord);

      assertEquals("Number of datasets: ", 4, mappingRepo.count());
      assertTrue(true);
    } catch (DataIntegrityViolationException dive) {
      assertTrue("No exception expected!", false);
    }

    // found 2 results for each mapping id
    Page<MappingRecord> page = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId), Arrays.asList((String) null), Pageable.unpaged());
    Page<MappingRecord> page_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId_2), Arrays.asList((String) null), Pageable.unpaged());
    assertEquals("Number of pages", 1, page.getTotalPages());
    assertEquals("Number of elements", 2, page.getTotalElements());
    assertEquals("Number of pages", 1, page_2.getTotalPages());
    assertEquals("Number of elements", 2, page_2.getTotalElements());
    // Found 2 results for each mapping type
    page = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList((String) null), Arrays.asList((String) mappingType), Pageable.unpaged());
    page_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList((String) null), Arrays.asList((String) mappingType_2), Pageable.unpaged());
    assertEquals("Number of pages", 1, page.getTotalPages());
    assertEquals("Number of elements", 2, page.getTotalElements());
    assertEquals("Number of pages", 1, page_2.getTotalPages());
    assertEquals("Number of elements", 2, page_2.getTotalElements());
    // No value if all arguments are null
    page = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList((String) null), Arrays.asList((String) null), Pageable.unpaged());
    assertEquals("Number of pages", 1, page.getTotalPages());
    assertEquals("Number of elements", 0, page.getTotalElements());

    // found 2 results for each mapping id with page size 1
    page = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId), Arrays.asList((String) null), PageRequest.of(0, 1));
    page_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId_2), Arrays.asList((String) null), PageRequest.of(0, 1));
    assertEquals("Number of pages", 2, page.getTotalPages());
    assertEquals("Number of elements", 2, page.getTotalElements());
    assertEquals("Content", 1, page.getContent().size());
    assertEquals("Number of pages", 2, page_2.getTotalPages());
    assertEquals("Number of elements", 2, page_2.getTotalElements());
    assertEquals("Content", 1, page_2.getContent().size());

    // found no results for second page with page size 2
    page = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId), Arrays.asList((String) null), PageRequest.of(1, 2));
    page_2 = mappingRepo.findByMappingIdInOrMappingTypeIn(Arrays.asList(mappingId_2), Arrays.asList((String) null), PageRequest.of(1, 2));
    assertEquals("Number of pages", 1, page.getTotalPages());
    assertEquals("Number of elements", 2, page.getTotalElements());
    assertEquals("Content", 0, page.getContent().size());
    assertEquals("Number of pages", 1, page_2.getTotalPages());
    assertEquals("Number of elements", 2, page_2.getTotalElements());
    assertEquals("Content", 0, page_2.getContent().size());
  }

  private List<String> StringToList(String x) {
    return Arrays.asList(x);
  }
}
