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
    private final static Logger LOGGER = LoggerFactory.getLogger(ShellRunnerUtil.class);

    public static MappingPluginState run(String[] command) throws MappingPluginException {
        return run(command, TIMEOUT);
    }

    public static MappingPluginState run(String[] command, int timeOutInSeconds) throws MappingPluginException {
        return run(command, System.out, System.err, timeOutInSeconds);
    }

    public static MappingPluginState run(String[] command, OutputStream output, OutputStream error) throws MappingPluginException {
        return run(command, output, error, TIMEOUT);
    }

    public static MappingPluginState run(String[] command, OutputStream output, OutputStream error, int timeOutInSeconds) throws MappingPluginException {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        int result;
        MappingPluginState returnValue = MappingPluginState.SUCCESS;

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            Future<List<String>> errorFuture = pool.submit(new ShellRunnerUtil.ProcessReadTask(p.getErrorStream()));
            Future<List<String>> inputFuture = pool.submit(new ShellRunnerUtil.ProcessReadTask(p.getInputStream()));

            List<String> stdErr = errorFuture.get(timeOutInSeconds, TimeUnit.SECONDS);
            List<String> stdOut = inputFuture.get(timeOutInSeconds, TimeUnit.SECONDS);

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

            result = p.waitFor();
            if (result != 0) {
                throw new ExecutionException(new Throwable());
            }
        } catch (IOException ioe) {
            LOGGER.error("Failed to execute python.", ioe);
            returnValue = MappingPluginState.EXECUTION_ERROR;
        } catch (TimeoutException te) {
            LOGGER.error("Python script did not return in expected timeframe of " + TIMEOUT + " seconds", te);
            returnValue = MappingPluginState.TIMEOUT;
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Failed to execute python script due to an unknown Exception.", e);
            returnValue = MappingPluginState.UNKNOWN_ERROR;
        } finally {
            pool.shutdown();
            if (returnValue != MappingPluginState.SUCCESS) throw new MappingPluginException(returnValue);
        }
        return returnValue;
    }

    private static class ProcessReadTask implements Callable<List<String>> {
        private final InputStream inputStream;

        public ProcessReadTask(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public List<String> call() {
            return new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.toList());
        }
    }
}