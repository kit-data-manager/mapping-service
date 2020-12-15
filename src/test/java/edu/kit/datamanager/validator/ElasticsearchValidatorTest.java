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

import java.io.IOException;
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
public class ElasticsearchValidatorTest {
  
  public ElasticsearchValidatorTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException {
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
   * Test of isValid method, of class ElasticsearchValidator.
   */
  @Test
  public void testIsValid() throws MalformedURLException {
    System.out.println("isValid");
    URL value = new URL("http://localhost:9200");
    ConstraintValidatorContext context = null;
    ElasticsearchValidator instance = new ElasticsearchValidator();
    boolean expResult = true;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsInvalidUrl() throws MalformedURLException {
    System.out.println("testIsInvalidUrl");
    URL value = new URL("http://localhost:9201");
    ConstraintValidatorContext context = null;
    ElasticsearchValidator instance = new ElasticsearchValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new URL("http://localhost:9200/");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsNull() throws MalformedURLException {
    System.out.println("testIsNull");
    URL value = null;
    ConstraintValidatorContext context = null;
    ElasticsearchValidator instance = new ElasticsearchValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsNotURL() throws MalformedURLException {
    System.out.println("testIsNotURL");
    // space is not allowed
    URL value = new URL("file: src/test/resources/examples/gemma/simple.json");
    ConstraintValidatorContext context = null;
    ElasticsearchValidator instance = new ElasticsearchValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsNotURL2() throws MalformedURLException {
    System.out.println("testIsNotURL");
    URL value = new URL("file:src/test/resources/examples/gemma/simple.json");
    ConstraintValidatorContext context = null;
    ElasticsearchValidator instance = new ElasticsearchValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  
}
