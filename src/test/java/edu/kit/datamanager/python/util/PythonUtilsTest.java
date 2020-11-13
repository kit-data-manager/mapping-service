/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.python.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 */
public class PythonUtilsTest {
  
  private static String PYTHON_EXECUTABLE;

  public PythonUtilsTest() {
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

  /**
   * Test of run method, of class PythonUtils.
   */
  @Test
  public void testRun_3args_withWrongClass() {
    System.out.println("testRun_3args_withWrongClass");
    String pythonLocation = PYTHON_EXECUTABLE;
    String scriptLocation = new File("src/test/resources/python/invalid.py").getAbsolutePath();
    String[] arguments = null;
    int expResult = PythonUtils.EXECUTION_ERROR;
    int result = PythonUtils.run(pythonLocation, scriptLocation, arguments);
    assertEquals(Integer.valueOf(expResult), Integer.valueOf(result));
  }

  /**
   * Test of run method, of class PythonUtils.
   */
  @Test
  public void testRun_3args_withTimeout() {
    System.out.println("testRun_3args_withTimeout");
    String pythonLocation = PYTHON_EXECUTABLE;
    String scriptLocation = new File("src/test/resources/python/sleep.py").getAbsolutePath();
    String[] arguments = null;
    int expResult = PythonUtils.TIMEOUT_ERROR;
    int result = PythonUtils.run(pythonLocation, scriptLocation, 1, arguments);
    assertEquals(Integer.valueOf(expResult), Integer.valueOf(result));
    expResult = PythonUtils.SUCCESS;
    result = PythonUtils.run(pythonLocation, scriptLocation, 5, arguments);
    assertEquals(Integer.valueOf(expResult), Integer.valueOf(result));

  }

  /**
   * Test of run method, of class PythonUtils.
   */
  @Test
  public void testRun_3args_withNoOutputStreams() {
    System.out.println("testRun_3args_withTimeout");
    String pythonLocation = PYTHON_EXECUTABLE;
    String scriptLocation = new File("src/test/resources/python/printOutput.py").getAbsolutePath();
    String[] arguments = null;
    int expResult = PythonUtils.SUCCESS;
    int result = PythonUtils.run(pythonLocation, scriptLocation, null, null, arguments);
    assertEquals(Integer.valueOf(expResult), Integer.valueOf(result));
  }

  /**
   * Test of run method, of class PythonUtils.
   */
  @Test
  public void testRun_3args_withInvalidPython() {
    System.out.println("testRun_3args_withInvalidPython");
    String pythonLocation = PYTHON_EXECUTABLE;
    String scriptLocation = "/notExistingFile.py";
    String[] arguments = null;
    int expResult = PythonUtils.EXECUTION_ERROR;
    int result = PythonUtils.run(pythonLocation, scriptLocation, arguments);
    assertEquals(Integer.valueOf(expResult), Integer.valueOf(result));
  }

}
