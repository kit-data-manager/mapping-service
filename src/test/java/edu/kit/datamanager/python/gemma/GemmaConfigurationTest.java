package edu.kit.datamanager.python.gemma;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import edu.kit.datamanager.python.gemma.GemmaConfiguration;
import edu.kit.datamanager.python.gemma.GemmaUtil;
import edu.kit.datamanager.python.util.PythonUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;

@RunWith(SpringRunner.class)
@ContextConfiguration
@AutoConfigureMockMvc
@TestExecutionListeners(listeners = {ServletTestExecutionListener.class,
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class,
  WithSecurityContextTestExecutionListener.class})
public class GemmaConfigurationTest {
  
  private static String PYTHON_EXECUTABLE;

  @Autowired
  GemmaUtil util;
  @Autowired
  GemmaConfiguration gemmaConfiguration;
  
  public GemmaConfigurationTest() {
  }
  
  @BeforeClass
  public static void setUpClass() throws IOException {
    // Determine python location
    OutputStream os = new ByteArrayOutputStream();
    PythonUtils.run("which","python3", os, null);
    String pythonExecutable = os.toString();
    os.flush();
    if (pythonExecutable.isBlank()) {
    PythonUtils.run("which","python", os, null);
     pythonExecutable = os.toString();
    }
    if (pythonExecutable.isBlank()) { 
      throw new IOException("Python seems not to be available!");
    }
    System.out.println("Location of python: " + pythonExecutable);
    PYTHON_EXECUTABLE = pythonExecutable.trim();
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
   * Test of getPythonLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testGetPythonLocation() {
    System.out.println("getPythonLocation");
    GemmaConfiguration instance = gemmaConfiguration;
    String expResult = PYTHON_EXECUTABLE;
    String result = instance.getPythonLocation();
    assertEquals(expResult, result);
  }

  /**
   * Test of getGemmaLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testGetGemmaLocation() {
    System.out.println("getGemmaLocation");
    GemmaConfiguration instance = new GemmaConfiguration();
    String expResult = "";
    String result = instance.getGemmaLocation();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setPythonLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testSetPythonLocation() {
    System.out.println("setPythonLocation");
    String pythonLocation = "";
    GemmaConfiguration instance = new GemmaConfiguration();
    instance.setPythonLocation(pythonLocation);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of setGemmaLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testSetGemmaLocation() {
    System.out.println("setGemmaLocation");
    String gemmaLocation = "";
    GemmaConfiguration instance = new GemmaConfiguration();
    instance.setGemmaLocation(gemmaLocation);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of equals method, of class GemmaConfiguration.
   */
  @Test
  public void testEquals() {
    System.out.println("equals");
    Object o = null;
    GemmaConfiguration instance = new GemmaConfiguration();
    boolean expResult = false;
    boolean result = instance.equals(o);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of canEqual method, of class GemmaConfiguration.
   */
  @Test
  public void testCanEqual() {
    System.out.println("canEqual");
    Object other = null;
    GemmaConfiguration instance = new GemmaConfiguration();
    boolean expResult = false;
    boolean result = instance.canEqual(other);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of hashCode method, of class GemmaConfiguration.
   */
  @Test
  public void testHashCode() {
    System.out.println("hashCode");
    GemmaConfiguration instance = new GemmaConfiguration();
    int expResult = 0;
    int result = instance.hashCode();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class GemmaConfiguration.
   */
  @Test
  public void testToString() {
    System.out.println("toString");
    GemmaConfiguration instance = new GemmaConfiguration();
    String expResult = "";
    String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
}
