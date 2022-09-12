///*
// * Copyright 2020 hartmann-v.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package edu.kit.datamanager.mappingservice.mapping;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * @author hartmann-v
// */
//public class MappingTest {
//
//    /**
//     * Test of values method, of class Mapping.
//     */
//    @Test
//    public void testValues() {
//        System.out.println("values");
//        Mapping[] expResult = {Mapping.GEMMA};
//        Mapping[] result = Mapping.values();
//        assertArrayEquals(expResult, result);
//    }
//
//    /**
//     * Test of valueOf method, of class Mapping.
//     */
//    @Test
//    public void testValueOf() {
//        System.out.println("valueOf");
//        String string = "GEMMA";
//        Mapping expResult = Mapping.GEMMA;
//        Mapping result = Mapping.valueOf(string);
//        assertEquals(expResult, result);
//    }
//
//    /**
//     * Test of valueOf method, of class Mapping.
//     */
//    @Test
//    public void testValueOfInvalidValue() {
//        System.out.println("testValueOfInvalidValue");
//        String string = "Gemma";
//        try {
//            Mapping result = Mapping.valueOf(string);
//            assertTrue(false);
//        } catch (IllegalArgumentException iae) {
//            assertTrue(true);
//        }
//    }
//
//}
