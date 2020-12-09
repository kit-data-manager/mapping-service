/*
 * Copyright 2020 KIT
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
package edu.kit.datamanager.indexer.util;

import edu.kit.datamanager.validator.ElasticsearchIndexValidator;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 */
public class ElasticsearchUtil {

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchUtil.class);

  /**
   * Test URL for pointing to a running elasticsearch instance.
   *
   * @param elasticsearchURL the given URL to check for an elasticsearch
   * instance.I
   * @return true if server is available.
   */
  public static boolean testForElasticsearch(URL elasticsearchURL) {
    boolean validElasticSearchServer = false;
    if (elasticsearchURL != null) {
      String baseUrl = elasticsearchURL.toString();
      String accessUrl = baseUrl + "/_search";
      RestTemplate restTemplate = new RestTemplate();
      try {
        ResponseEntity<String> entity = restTemplate.getForEntity(accessUrl,
                String.class,
                baseUrl);
        LOGGER.info("Status code value: " + entity.getStatusCodeValue());
        LOGGER.info("HTTP Header 'ContentType': " + entity.getHeaders().getContentType());
        if (entity.getStatusCodeValue() == HttpStatus.OK.value()) {
          LOGGER.trace("Elasticsearch server at '{}' seems to be up and running!", baseUrl);
          validElasticSearchServer = true;
        }
      } catch (Exception ex) {
        LOGGER.error("Error accessing elasticsearch server!", ex);
      }
      // test for trailing '/'
      if (baseUrl.trim().endsWith("/")) {
        LOGGER.error("Please remove trailing '/' from URL '{}'!", baseUrl);
        validElasticSearchServer = false;
      }
      if (!validElasticSearchServer) {
        LOGGER.trace("URL seems to be invalid: '{}'!", baseUrl);
      }
    }
    return validElasticSearchServer;
  }

  /**
   * Test if string is a valid elasticsearch index. If not - change to lower
   * case - replace all invalid characters by '_'
   *
   * @param elasticsearchIndex
   * @return valid index
   */
  public static String testForValidIndex(String elasticsearchIndex) {
    String validIndex = elasticsearchIndex;

    boolean valid = new ElasticsearchIndexValidator().isValid(validIndex, null);
    if (!valid) {
      String pattern = "[" + ElasticsearchIndexValidator.SPECIAL_CHARACTERS + "]";
      validIndex = validIndex.toLowerCase().replaceAll(pattern, "_");
    }
    return validIndex;
  }

}
