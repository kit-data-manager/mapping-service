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
package edu.kit.datamanager.mappingservice.python.util;

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.util.PythonRunnerUtil;
import edu.kit.datamanager.mappingservice.util.ShellRunnerUtil;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 */
@SpringBootTest
@ActiveProfiles("test")
public class PythonUtilsTest {
    
    @Autowired
    private ApplicationProperties applicationProperties;
    
    public PythonUtilsTest() {
    }
    
    @BeforeEach
    public void setUpClass() {
        PythonRunnerUtil.init(applicationProperties);
    }

    /**
     * Test of run method, of class PythonUtils.
     */
    @Test
    public void testRun_3args_withWrongPython() {
        System.out.println("testRun_3args_withWrongPython");
        ApplicationProperties props = new ApplicationProperties();
        props.setPythonExecutable(null);
        PythonRunnerUtil.init(props);
        String scriptLocation = "";
        String[] arguments = null;
        try {
            MappingPluginState result = PythonRunnerUtil.runPythonScript(scriptLocation, arguments);
            fail("Expected MappingPluginException");
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.StateEnum.UNKNOWN_ERROR, e.getMappingPluginState().getState());
        }
    }

    /**
     * Test of run method, of class PythonUtils.
     */
    @Test
    public void testRun_3args_withWrongClass() {
        System.out.println("testRun_3args_withWrongClass");
        String scriptLocation = new File("src/test/resources/python/invalid.py").getAbsolutePath();
        String[] arguments = null;
        try {
            MappingPluginState state = PythonRunnerUtil.runPythonScript(scriptLocation, arguments);
            fail("Expected MappingPluginException");
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.StateEnum.BAD_EXIT_CODE, e.getMappingPluginState().getState());
            assertEquals(123, e.getMappingPluginState().getDetails());
        }
    }

    /**
     * Test of run method, of class PythonUtils.
     */
    @Test
    public void testRun_3args_withTimeout() {
        System.out.println("testRun_3args_withTimeout");
        String scriptLocation = new File("src/test/resources/python/sleep.py").getAbsolutePath();
        String[] arguments = null;
        ApplicationProperties props = new ApplicationProperties();
        props.setExecutionTimeout(1);
        ShellRunnerUtil.init(props);
        try {
            PythonRunnerUtil.runPythonScript(scriptLocation, arguments);
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.StateEnum.TIMEOUT, e.getMappingPluginState().getState());
        }
        
    }

    /**
     * Test of run method, of class PythonUtils.
     */
    @Test
    public void testRun_3args_withNoOutputStreams() {
        System.out.println("testRun_3args_withTimeout");
        String scriptLocation = new File("src/test/resources/python/printOutput.py").getAbsolutePath();
        String[] arguments = null;
        try {
            PythonRunnerUtil.runPythonScript(scriptLocation, null, null, arguments);
            fail("Expected MappingPluginException");
        } catch (MappingPluginException e) {
            assertEquals(MappingPluginState.StateEnum.INVALID_INPUT, e.getMappingPluginState().getState());
        }
    }
}
