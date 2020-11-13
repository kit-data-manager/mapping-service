/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.python.gemma;

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
public class GemmaConfigurationTest {
  
  public GemmaConfigurationTest() {
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
   * Test of getPythonLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testSetAndGetPythonLocation() {
    System.out.println("getPythonLocation");
    GemmaConfiguration instance = new GemmaConfiguration();
    String expResult = null;
    String result = instance.getPythonLocation();
    assertEquals(expResult, result);
    expResult = "pythonLocation";
    instance.setPythonLocation(expResult);
    result = instance.getPythonLocation();
    assertEquals(expResult, result);
  }

  /**
   * Test of getGemmaLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testSetAndGetGemmaLocation() {
    System.out.println("getGemmaLocation");
    GemmaConfiguration instance = new GemmaConfiguration();
     String expResult = null;
    String result = instance.getGemmaLocation();
    assertEquals(expResult, result);
    expResult = "pythonLocation";
    instance.setGemmaLocation(expResult);
    result = instance.getGemmaLocation();
    assertEquals(expResult, result);
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
    expResult = true;
    result = instance.equals(new GemmaConfiguration());
    assertEquals(expResult, result);
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
  
}
