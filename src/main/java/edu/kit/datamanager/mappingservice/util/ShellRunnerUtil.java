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
import edu.kit.datamanager.mappingservice.exception.BadExitCodeException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for running shell scripts.
 *
 * @author maximilianiKIT
 */
public class ShellRunnerUtil {

    private static ApplicationProperties configuration;

    /**
     * Logger for this class.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ShellRunnerUtil.class);

    public static void init(ApplicationProperties configuration) {
        ShellRunnerUtil.configuration = configuration;
    }

    /**
     * This method executes a shell command.
     *
     * @param command The command to execute without spaces.
     * @return State of the execution.
     * @throws MappingPluginException If an error occurs.
     */
    public static MappingPluginState run(String... command) throws MappingPluginException {
        return run(configuration.getExecutionTimeout(), command);
    }

    /**
     * This method executes a shell command.
     *
     * @param timeOutInSeconds Time in seconds when the script should throw a
     * timeout exception.
     * @param command The command to execute without spaces.
     * @return State of the execution.
     * @throws MappingPluginException If an error occurs.
     */
    public static MappingPluginState run(int timeOutInSeconds, String... command) throws MappingPluginException {
        return run(System.out, System.err, timeOutInSeconds, command);
    }

    /**
     * This method executes a shell command and writes the output and errors to
     * the given streams.
     *
     * @param output OutputStream to redirect the output to.
     * @param error OutputStream to redirect the errors to.
     * @param command The command to execute without spaces.
     * @return State of the execution.
     * @throws MappingPluginException If an error occurs.
     */
    public static MappingPluginState run(OutputStream output, OutputStream error, String... command) throws MappingPluginException {
        return run(output, error, configuration.getExecutionTimeout(), command);
    }

    /**
     * This method executes a shell command and writes the output and errors to
     * the given streams.
     *
     * @param output OutputStream to redirect the output to.
     * @param error OutputStream to redirect the errors to.
     * @param timeOutInSeconds Time in seconds when the script should throw a
     * timeout exception.
     * @param command The command to execute without spaces.
     *
     * @return State of the execution only if execution was successful.
     * Otherwise, MappingPluginException is thrown.
     *
     * @throws MappingPluginException If an error occurs.
     */
    public static MappingPluginState run(OutputStream output, OutputStream error, int timeOutInSeconds, String... command) throws MappingPluginException {
        if (output == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT(), "Output stream is null.");
        }
        if (error == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT(), "Error stream is null.");
        }
        if (timeOutInSeconds <= 0) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT(), "Execution timeout is leq 0.");
        }
        if (command == null || command.length == 0) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT(), "No command given.");
        }

        LOGGER.trace("Running command {} with timeout {}.", Arrays.asList(command), timeOutInSeconds);

        MappingPluginState returnValue = MappingPluginState.SUCCESS();
        ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            LOGGER.trace("Starting process.");
            Process p = pb.start();
            LOGGER.trace("Connecting streams.");

            // Pipe stdout to provided OutputStream
            Thread stdoutThread = new Thread(() -> {
                try (InputStream is = p.getInputStream()) {
                    is.transferTo(output);
                } catch (IOException e) {
                    LOGGER.error("Error piping stdout", e);
                }
            });

            // Buffer stderr into ByteArrayOutputStream
            Thread stderrThread = new Thread(() -> {
                try (InputStream is = p.getErrorStream()) {
                    is.transferTo(errorBuffer);
                } catch (IOException e) {
                    LOGGER.error("Error buffering stderr", e);
                }
            });

            stdoutThread.start();
            stderrThread.start();

            LOGGER.trace("Waiting for process to finish.");
            if (!p.waitFor(timeOutInSeconds, TimeUnit.SECONDS)) {
                throw new TimeoutException("Process did not return within " + timeOutInSeconds + " seconds.");
            }
            stdoutThread.join(); // wait for streams to finish
            stderrThread.join();

            LOGGER.trace("Checking exit value.");
            if (p.exitValue() != 0) {
                throw new BadExitCodeException(p.exitValue());
            }
        } catch (IOException ioe) {
            LOGGER.error("Failed to run command or to access output/error streams.", ioe);
            returnValue = MappingPluginState.EXECUTION_ERROR();
            returnValue.setDetails("Failed to run command or to access output/error streams.");
        } catch (TimeoutException te) {
            LOGGER.error("Command did not return in expected timeframe of {} seconds", timeOutInSeconds, te);
            returnValue = MappingPluginState.TIMEOUT();
            returnValue.setDetails(te.getMessage());
        } catch (InterruptedException e) {
            LOGGER.error("Command execution has been interrupted.", e);
            returnValue = MappingPluginState.UNKNOWN_ERROR();
            returnValue.setDetails("Command execution has been interrupted.");
        } catch (BadExitCodeException e) {
            LOGGER.error("Failed to execute command due to an unexpected exception.", e);
            returnValue = MappingPluginState.BAD_EXIT_CODE();
            returnValue.setDetails("Mapping process returned with exit code " + e.getExitCode() + ". StdErr:\n" + errorBuffer);
        } finally {
            //write output puffer to provided output stream
            LOGGER.trace("Process finished. Forwarding collected error output \n{}\nto provided error stream.", errorBuffer);
            try {
                error.write(errorBuffer.toByteArray());
                error.flush();
            }catch(IOException ioe){
                LOGGER.info("Failed to write error buffer to error stream.", ioe);
            }
        }

        if (returnValue.getState() != MappingPluginState.SUCCESS().getState()) {
            throw new MappingPluginException(returnValue);
        }
        return returnValue;
    }
}
