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


}
