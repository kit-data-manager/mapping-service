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
package edu.kit.datamanager.validator;

import edu.kit.datamanager.annotations.ExecutableFileURL;
import java.net.URISyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 */
public class ExecutableFileValidator implements ConstraintValidator<ExecutableFileURL, java.net.URL> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableFileValidator.class);

  @Override
  public boolean isValid(java.net.URL value, ConstraintValidatorContext context) {
    boolean validExecutableFile = false;
    if (value == null) {
      LOGGER.error("Provided value is null.");
      return validExecutableFile;
    }
    try {
      LOGGER.trace("Successfully validated file URL {}. Checking local path.", value.toURI().toString());
      File executableFile = new File(value.getPath());
      String pathToExecutableFile = executableFile.getAbsolutePath();
      if (!executableFile.exists()) {
        LOGGER.error("File at {} does not exist!", pathToExecutableFile);
      } else {
        if (!executableFile.isFile()) {
          LOGGER.error("File at {} is not a file!", pathToExecutableFile);
        } else {
          if (!executableFile.canExecute()) {
            LOGGER.error("File at {} is not executable!", pathToExecutableFile);
          } else {
            LOGGER.trace("File at {} exists and is executable.", pathToExecutableFile);
            validExecutableFile = true;
          }
        }
      }
    } catch (URISyntaxException ex) {
      LOGGER.error("Failed to validate property with value " + value + ". -> Not a valid URL!", ex);
    }
    return validExecutableFile;
  }
}
