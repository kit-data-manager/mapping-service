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

/**
 * Utility class for running shell scripts.
 *
 * @author maximilianiKIT
 */
public class ShellRunnerUtil {

  /**
   * Time in seconds when the script should throw a timeout exception.
   */
  public static final int TIMEOUT = 30;

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(ShellRunnerUtil.class);

  /**
   * This method executes a shell command.
   *
   * @param command The command to execute without spaces.
   * @return State of the execution.
   * @throws MappingPluginException If an error occurs.
   */
  public static MappingPluginState run(String... command) throws MappingPluginException {
    return run(TIMEOUT, command);
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
    return run(output, error, TIMEOUT, command);
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
   * @return State of the execution.
   * @throws MappingPluginException If an error occurs.
   */
  public static MappingPluginState run(OutputStream output, OutputStream error, int timeOutInSeconds, String... command) throws MappingPluginException {
    if (output == null) {
      throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Output stream is null.");
    }
    if (error == null) {
      throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Error stream is null.");
    }
    if (timeOutInSeconds <= 0) {
      throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "Timeout is null or negative.");
    }
    if (command == null || command.length == 0) {
      throw new MappingPluginException(MappingPluginState.INVALID_INPUT, "No command given.");
    }

    ExecutorService pool = null;
    int result;
    MappingPluginState returnValue = MappingPluginState.SUCCESS;

    try {
      pool = Executors.newSingleThreadExecutor();

      ProcessBuilder pb = new ProcessBuilder(command);
      Process p = pb.start();

      Future<List<String>> errorFuture = pool.submit(new ShellRunnerUtil.ProcessReadTask(p.getErrorStream()));
      Future<List<String>> inputFuture = pool.submit(new ShellRunnerUtil.ProcessReadTask(p.getInputStream()));

      List<String> stdErr = errorFuture.get(timeOutInSeconds, TimeUnit.SECONDS);
      List<String> stdOut = inputFuture.get(timeOutInSeconds, TimeUnit.SECONDS);

      for (String line : stdOut) {
        LOGGER.trace("[OUT] {}", line);
        output.write((line + "\n").getBytes());
      }

      for (String line : stdErr) {
        LOGGER.trace("[ERR] {}", line);
        error.write((line + "\n").getBytes());
      }

      result = p.waitFor();
      if (result != 0) {
        throw new ExecutionException(new Throwable());
      }
    } catch (IOException ioe) {
      LOGGER.error("Failed to execute command.", ioe);
      returnValue = MappingPluginState.EXECUTION_ERROR;
    } catch (TimeoutException te) {
      LOGGER.error("Command did not return in expected timeframe of " + TIMEOUT + " seconds", te);
      returnValue = MappingPluginState.TIMEOUT;
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Failed to execute command due to an unknown exception.", e);
      returnValue = MappingPluginState.UNKNOWN_ERROR;
    } finally {
      if (pool != null) {
        pool.shutdown();
        pool.close();
      }
    }
    if (returnValue != MappingPluginState.SUCCESS) {
      throw new MappingPluginException(returnValue);
    }
    return returnValue;
  }

  private record ProcessReadTask(InputStream inputStream) implements Callable<List<String>> {

    @Override
    public List<String> call() {
      return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList());
    }
  }
}
