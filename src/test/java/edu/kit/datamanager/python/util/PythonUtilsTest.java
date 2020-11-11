/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.python.util;

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
public class PythonUtilsTest {

  public PythonUtilsTest() {
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
   * Test of run method, of class PythonUtils.
   */
  @Test
  public void testRun_3args_withWrongPython() {
    System.out.println("testRun_3args_withWrongPython");
    String pythonLocation = "/usr/bin/invalidpython";
    String scriptLocation = "";
    String[] arguments = null;
    int expResult = PythonUtils.PYTHON_NOT_FOUND_ERROR;
    int result = PythonUtils.run(pythonLocation, scriptLocation, arguments);
    assertEquals(Integer.valueOf(expResult), Integer.valueOf(result));
  }

}
