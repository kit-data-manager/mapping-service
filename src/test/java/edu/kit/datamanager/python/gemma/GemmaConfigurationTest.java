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
}
