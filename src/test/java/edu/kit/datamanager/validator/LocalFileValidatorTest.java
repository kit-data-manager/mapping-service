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
package edu.kit.datamanager.validator;

import edu.kit.datamanager.python.util.PythonUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.validation.ConstraintValidatorContext;
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
public class LocalFileValidatorTest {
  
  private static URL PYTHON_EXECUTABLE;
  
  public LocalFileValidatorTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException {
    // Determine python location
    OutputStream os = new ByteArrayOutputStream();
    PythonUtils.run("which", "python3", os, null);
    String pythonExecutable = os.toString();
    os.flush();
    if (pythonExecutable.trim().isEmpty()) {
      PythonUtils.run("which", "python", os, null);
      pythonExecutable = os.toString();
    }
    if (pythonExecutable.trim().isEmpty()) {
      throw new IOException("Python seems not to be available!");
    }
    System.out.println("Location of python: " + pythonExecutable);
    PYTHON_EXECUTABLE = new File(pythonExecutable.trim()).toURI().toURL();
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
   * Test of isValid method, of class LocalFileValidator.
   */
  @Test
  public void testIsValid() throws MalformedURLException {
    System.out.println("isValid");
    URL value = PYTHON_EXECUTABLE;
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = true;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsInvalidFile() throws MalformedURLException {
    System.out.println("testIsInvalidFile");
    URL value = new URL("file:invalid/file");
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsNull() throws MalformedURLException {
    System.out.println("testIsNull");
    URL value = null;
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsPath() throws MalformedURLException {
    System.out.println("testIsPath");
    URL value = new URL("file:///tmp");
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsNotURL() throws MalformedURLException {
    System.out.println("testIsNotURL");
    URL value = new URL("file: src/test/resources/examples/gemma/simple.json");
    ConstraintValidatorContext context = null;
    LocalFileValidator instance = new LocalFileValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  
}
