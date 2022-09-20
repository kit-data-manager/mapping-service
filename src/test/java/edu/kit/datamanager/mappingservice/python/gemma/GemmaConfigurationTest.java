///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package edu.kit.datamanager.mappingservice.python.gemma;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
///**
// * @author hartmann-v
// */
//public class GemmaConfigurationTest {
//
//    /**
//     * Test of getPythonLocation method, of class GemmaConfiguration.
//     */
//    @Test
//    public void testSetAndGetPythonLocation() throws MalformedURLException {
//        System.out.println("getPythonLocation");
//        GemmaConfiguration instance = new GemmaConfiguration();
//        URL expResult = null;
//        URL result = instance.getPythonLocation();
//        assertEquals(expResult, result);
//        expResult = new URL("file:pythonLocation");
//        instance.setPythonLocation(expResult);
//        result = instance.getPythonLocation();
//        assertEquals(expResult, result);
//    }
//
//    /**
//     * Test of getGemmaLocation method, of class GemmaConfiguration.
//     */
//    @Test
//    public void testSetAndGetGemmaLocation() throws MalformedURLException {
//        System.out.println("getGemmaLocation");
//        GemmaConfiguration instance = new GemmaConfiguration();
//        URL expResult = null;
//        URL result = instance.getGemmaLocation();
//        assertEquals(expResult, result);
//        expResult = new URL("file:pythonLocation");
//        instance.setGemmaLocation(expResult);
//        result = instance.getGemmaLocation();
//        assertEquals(expResult, result);
//    }
//}
