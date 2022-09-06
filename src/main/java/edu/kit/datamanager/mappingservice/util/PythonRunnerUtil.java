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

import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class PythonRunnerUtil {

    private URL pythonLocation;

    public PythonRunnerUtil(ApplicationProperties configuration) throws MalformedURLException {
        pythonLocation = new File(configuration.getPythonLocation().getPath()).toURI().toURL();
        printPythonVersion();
    }

    public void printPythonVersion() {
        try {
            ShellRunnerUtil.runShellCommand(pythonLocation.getPath() + " --version");
        } catch (MappingPluginException e) {
            e.printStackTrace();
        }
    }

}
