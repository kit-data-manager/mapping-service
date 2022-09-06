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
import edu.kit.datamanager.mappingservice.plugins.MappingPluginStates;
import edu.kit.datamanager.mappingservice.python.util.PythonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ShellRunnerUtil {

    /**
     * Time in seconds when the script should throw a timeout exception.
     */
    public static final int TIMEOUT = 30;

    /**
     * Logger for this class.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(PythonUtils.class);

    public static MappingPluginStates runShellCommand(String command) throws MappingPluginException {
        return runShellCommand(command, System.out, System.err);
    }
    public static MappingPluginStates runShellCommand(String command, OutputStream output, OutputStream error) throws MappingPluginException {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        MappingPluginStates state = MappingPluginStates.SUCCESS;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            Future<List<String>> errorFuture = pool.submit(new ShellRunnerUtil.ProcessReadTask(p.getErrorStream()));
            Future<List<String>> inputFuture = pool.submit(new ShellRunnerUtil.ProcessReadTask(p.getInputStream()));

            List<String> stdErr = errorFuture.get(TIMEOUT, TimeUnit.SECONDS);
            List<String> stdOut = inputFuture.get(TIMEOUT, TimeUnit.SECONDS);

            for (String line : stdOut) {
                LOGGER.trace("[OUT] {}", line);
                if (output != null) {
                    output.write((line + "\n").getBytes());
                }
            }

            for (String line : stdErr) {
                LOGGER.trace("[ERR] {}", line);
                if (error != null) {
                    error.write((line + "\n").getBytes());
                }
            }

            int result = p.waitFor();
            if (result != 0) {
                throw new ExecutionException(new Throwable());
            }
        } catch (IOException ioe) {
            LOGGER.error("Failed to execute python.", ioe);
            state = MappingPluginStates.EXECUTION_ERROR;
            throw new MappingPluginException(MappingPluginStates.EXECUTION_ERROR, "failed to execute python");
        } catch (TimeoutException te) {
            LOGGER.error("Python script did not return in expected timeframe of " + TIMEOUT + " seconds", te);
            state = MappingPluginStates.TIMEOUT;
            throw new MappingPluginException(MappingPluginStates.TIMEOUT, "Python script did not return in expected timeframe of " + TIMEOUT + " seconds");
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to execute python script due to an unknown Exception.", e);
            state = MappingPluginStates.UNKNOWN_ERROR;
            throw new MappingPluginException(MappingPluginStates.UNKNOWN_ERROR, "Failed to execute python script due to an unknown Exception.", e);
        } finally {
            pool.shutdown();
        }
        return state;
    }

    private record ProcessReadTask(InputStream inputStream) implements Callable<List<String>> {

        @Override
        public List<String> call() {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.toList());
        }
    }
}
