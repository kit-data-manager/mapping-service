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
public class ElasticsearchIndexValidatorTest {
  
  public ElasticsearchIndexValidatorTest() {
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
   * Test of isValid method, of class ElasticsearchIndexValidator.
   */
  @Test
  public void testIsValid() {
    System.out.println("isValid");
    String value = new String("valid_with_small_letters");
    ConstraintValidatorContext context = null;
    ElasticsearchIndexValidator instance = new ElasticsearchIndexValidator();
    boolean expResult = true;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  @Test
  public void testIsInvalidIndex() {
    System.out.println("testIsInvalidUrl");
    String value = new String("testBigLetter");
    ConstraintValidatorContext context = null;
    ElasticsearchIndexValidator instance = new ElasticsearchIndexValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial\"character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial*character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial<character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial>character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial|character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial,character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial/character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial?character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial{character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial}character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial[character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial]character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
    value = new String("testspecial`character");
    result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }

  @Test
  public void testIsNull() {
    System.out.println("testIsNull");
    String value = null;
    ConstraintValidatorContext context = null;
    ElasticsearchIndexValidator instance = new ElasticsearchIndexValidator();
    boolean expResult = false;
    boolean result = instance.isValid(value, context);
    assertEquals(expResult, result);
  }
  
}
