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

import edu.kit.datamanager.annotations.ElasticsearchIndex;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ElasticsearchIndexValidator implements ConstraintValidator<ElasticsearchIndex, String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchIndexValidator.class);
  
  public static final String SPECIAL_CHARACTERS = "  \"*\\<|,>/?{}\\[\\]`A-Z";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    boolean validElasticsearchIndex = false;
    if (value == null) {
      LOGGER.error("Provided value is null.");
      return validElasticsearchIndex;
    }
    // index mustn't contain the following special character;
    Pattern p = Pattern.compile("[" + SPECIAL_CHARACTERS + "]");
    Matcher m = p.matcher(value);

    if (m.find()) {
      LOGGER.error("Index must not contain one of the following characters: '{}'!", SPECIAL_CHARACTERS);
    } else {
      validElasticsearchIndex = true;
    }

    return validElasticsearchIndex;
  }
}
