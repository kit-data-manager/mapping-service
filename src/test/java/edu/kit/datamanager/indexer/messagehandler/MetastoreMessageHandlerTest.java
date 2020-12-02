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
package edu.kit.datamanager.indexer.messagehandler;

import edu.kit.datamanager.entities.messaging.BasicMessage;
import edu.kit.datamanager.entities.messaging.DataResourceMessage;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.dao.IMappingRecordDao;
import edu.kit.datamanager.indexer.service.impl.MappingService;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler.RESULT;
import edu.kit.datamanager.util.AuthenticationHelper;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 */
@RunWith(SpringRunner.class)
@PowerMockRunnerDelegate(SpringJUnit4ClassRunner.class)
@PowerMockIgnore({"javax.crypto.*", "javax.management.*"})
@PrepareForTest(AuthenticationHelper.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {
  DependencyInjectionTestExecutionListener.class,
  TransactionalTestExecutionListener.class
})
@ActiveProfiles("test")
public class MetastoreMessageHandlerTest {

  @Autowired
  ApplicationProperties applicationProperties;

  @Autowired
  IMappingRecordDao mappingRepo;

  @Autowired
  MappingService mappingService4Test;

  public MetastoreMessageHandlerTest() {
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
   * Test of handle method, of class MetastoreMessageHandler.
   */
  @Test
  public void testHandle() {
    System.out.println("handle");
    Map<String, String> map = new HashMap<>();
    BasicMessage message = new DataResourceMessage();
    message.setMetadata(map);
    message.setAction("CREATE");
    MetastoreMessageHandler instance = new MetastoreMessageHandler(applicationProperties, mappingService4Test);
    RESULT expResult = RESULT.REJECTED;
    RESULT result = instance.handle(message);
    assertEquals(expResult, result);
    map.put("resolvingUrl", "anyUrl");
    result = instance.handle(message);
    assertEquals(expResult, result);
    map.put("schemaId", "anyId");
    expResult = RESULT.REJECTED;
    result = instance.handle(message);
    assertEquals(expResult, result);
  }

  /**
   * Test of configure method, of class MetastoreMessageHandler.
   */
  @Test
  public void testConfigure() {
    System.out.println("configure");
    MetastoreMessageHandler instance = new MetastoreMessageHandler(applicationProperties, mappingService4Test);
    boolean expResult = true;
    boolean result = instance.configure();
    assertEquals(expResult, result);
  }

}
