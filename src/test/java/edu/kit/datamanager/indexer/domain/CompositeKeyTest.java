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
package edu.kit.datamanager.indexer.domain;

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
public class CompositeKeyTest {
  
  public CompositeKeyTest() {
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
   * Test of hashCode method, of class CompositeKey.
   */
  @Test
  public void testHashCode() {
    System.out.println("hashCode");
    CompositeKey instance = new CompositeKey();
    int expResult = 10443;
    int result = instance.hashCode();
    assertEquals(expResult, result);
  }

  /**
   * Test of equals method, of class CompositeKey.
   */
  @Test
  public void testEquals() {
    System.out.println("equals");
    Object obj = null;
    CompositeKey instance = new CompositeKey();
    boolean expResult = false;
    boolean result = instance.equals(obj);
    assertEquals(expResult, result);
    obj = new Object();
    result = instance.equals(obj);
    assertEquals(expResult, result);
    CompositeKey comp2 = new CompositeKey();
    comp2.mappingId = "map";
    result = instance.equals(comp2);
    assertEquals(expResult, result);
    comp2.mappingId = null;
   comp2.mappingType = "type";
    result = instance.equals(comp2);
    assertEquals(expResult, result);
    comp2.mappingId = "map";
   comp2.mappingType = "type";
    result = instance.equals(comp2);
    assertEquals(expResult, result);
    expResult=true;
    instance.mappingId = "map";
    instance.mappingType = "type";
    result = instance.equals(comp2);
    assertEquals(expResult, result);
    comp2 = instance;
    result = instance.equals(comp2);
    assertEquals(expResult, result);
  }
  
}
