/*
 * Copyright 2019 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.mappingservice.indexer.exception;


import edu.kit.datamanager.mappingservice.exception.MappingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 */
public class MappingExceptionTest {

  @Test
  public void testAllConstructors() {
    String messageOne = "any message";
    String messageTwo = "any other message";
    MappingException exception = new MappingException();
    assertNotNull(exception);
    assertNull(exception.getMessage());
    MappingException exceptionWithMessage = new MappingException(messageOne);
    assertNotNull(exceptionWithMessage);
    assertEquals(messageOne, exceptionWithMessage.getMessage());
    MappingException exceptionWithCause = new MappingException(exception);
    assertNotNull(exceptionWithCause);
    assertNull(exception.getMessage());
    assertEquals(exception, exceptionWithCause.getCause());
    MappingException exceptionWithMessageAndCause = new MappingException(messageTwo,exception);
    assertNotNull(exceptionWithMessageAndCause);
    assertNotNull(exceptionWithMessageAndCause.getMessage());
    assertEquals(exception, exceptionWithMessageAndCause.getCause());
    assertEquals(messageTwo, exceptionWithMessageAndCause.getMessage());
    
    assertTrue(true);
  }
  
}
