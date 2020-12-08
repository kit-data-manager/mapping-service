/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.python.gemma;

import java.net.MalformedURLException;
import java.net.URL;
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
  public void testSetAndGetPythonLocation() throws MalformedURLException {
    System.out.println("getPythonLocation");
    GemmaConfiguration instance = new GemmaConfiguration();
    URL expResult = null;
    URL result = instance.getPythonLocation();
    assertEquals(expResult, result);
    expResult = new URL("file:pythonLocation");
    instance.setPythonLocation(expResult);
    result = instance.getPythonLocation();
    assertEquals(expResult, result);
  }

  /**
   * Test of getGemmaLocation method, of class GemmaConfiguration.
   */
  @Test
  public void testSetAndGetGemmaLocation() throws MalformedURLException {
    System.out.println("getGemmaLocation");
    GemmaConfiguration instance = new GemmaConfiguration();
     URL expResult = null;
    URL result = instance.getGemmaLocation();
    assertEquals(expResult, result);
    expResult = new URL("file:pythonLocation");
    instance.setGemmaLocation(expResult);
    result = instance.getGemmaLocation();
    assertEquals(expResult, result);
  }
}
