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

import edu.kit.datamanager.annotations.LocalFileURL;
import java.net.URISyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

/**
 *
 */
public class LocalFileValidator implements ConstraintValidator<LocalFileURL, java.net.URL>{

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileValidator.class);

  @Override
  public boolean isValid(java.net.URL value, ConstraintValidatorContext context) {
    boolean validLocalFile = false;
    if (value == null) {
      LOGGER.error("Provided value is null.");
      return validLocalFile;
    }
    try {
      LOGGER.trace("Successfully validated file URL {}. Checking local path.", value.toURI().toString());
      File localFile = new File(value.getPath());
      String pathToLocalFile = localFile.getAbsolutePath();
      if (!localFile.exists()) {
        LOGGER.error("File at {} does not exist!", pathToLocalFile);
      } else {
        if (!localFile.isFile()) {
          LOGGER.error("File at {} is not a file!", pathToLocalFile);
        } else {
          if (!localFile.canRead()) {
            LOGGER.error("File at {} is not readable!", pathToLocalFile);
          } else {
            LOGGER.trace("File at {} exists and is executable.", pathToLocalFile);
            validLocalFile = true;
          }
        }
      }
    } catch (URISyntaxException ex) {
      LOGGER.error("Failed to validate folder property with value " + value + ". -> Not a valid URL!", ex);
    }
    return validLocalFile;
  }
}
