/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.util;

import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShellRunnerUtilTest {

    @Test
    void runValid() {
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                assertEquals(MappingPluginState.SUCCESS(), ShellRunnerUtil.run("echo.bat", "test"));
                assertEquals(MappingPluginState.SUCCESS(), ShellRunnerUtil.run(5, "echo.bat", "test"));
                assertEquals(MappingPluginState.SUCCESS(), ShellRunnerUtil.run(System.out, System.err, "echo.bat", "test"));
            } catch (MappingPluginException e) {
                fail(e);
            }
        } else {
            try {
                assertEquals(MappingPluginState.SUCCESS(), ShellRunnerUtil.run("echo", "test"));
                assertEquals(MappingPluginState.SUCCESS(), ShellRunnerUtil.run(5, "echo", "test"));
                assertEquals(MappingPluginState.SUCCESS(), ShellRunnerUtil.run(System.out, System.err, "echo", "test"));
            } catch (MappingPluginException e) {
                fail(e);
            }
        }
    }

    @Test
    void runInvalid() {
        assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(""));
        assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run("blablusdhflakjdsfh"));

        if (SystemUtils.IS_OS_WINDOWS) {
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(-5, "echo.bat", "test"));
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(null, System.err, "echo.bat", "test"));
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(System.err, null, "echo.bat", "test"));
        } else {
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(-5, "echo", "test"));
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(null, System.err, "echo", "test"));
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(System.err, null, "echo", "test"));
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(1, "cat", "/dev/urandom"));
            assertThrows(MappingPluginException.class, () -> ShellRunnerUtil.run(1, "sudo", "cat", "/dev/urandom"));
        }
    }
}
