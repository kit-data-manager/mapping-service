/*
 * Copyright 2018 Karlsruhe Institute of Technology.
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
package edu.kit.datamanager.indexer.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.mapping.MappingUtil;
import edu.kit.datamanager.indexer.util.ElasticsearchUtil;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for managing mappings.
 */
@Service
public class IndexingService {

  /**
   * Instance holding all settings.
   */
  private final ApplicationProperties applicationProperties;

  
  private RestTemplate restTemplate = new RestTemplate();
  
  private String baseUrl;

  /**
   * Logger for this class.
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(IndexingService.class);

  @Autowired
  public IndexingService(ApplicationProperties applicationProperties) {
    this.applicationProperties = applicationProperties;
    init(applicationProperties);
  }

  /**
   * Initalize mappings directory and mappingUtil instance.
   *
   * @param applicationProperties Properties holding mapping directory setting.
   */
  private void init(ApplicationProperties applicationProperties) {
    if ((applicationProperties != null) && (applicationProperties.getElasticsearchUrl()!= null)) {
      boolean testForElasticsearch = ElasticsearchUtil.testForElasticsearch(applicationProperties.getElasticsearchUrl());
      baseUrl = applicationProperties.getElasticsearchUrl().toString();
      if (!testForElasticsearch) {
        throw new IndexerException("Could not connect to elasticsearch using URL '" + baseUrl + "'!");
      }
    }
  }

//    /**
//     * Transforms a given PID to a filename where the record with this PID can be
//     * stored.
//     * 
//     * @param pid the given PID.
//     * @return if the PID was not empty or null, it will return a filename. Empty
//     *         otherwise.
//     */
//    private Optional<String> pidToFilename(String pid) {
//        if (pid == null || pid.isEmpty()) {
//            return Optional.empty();
//        }
////        pid = this.pidToSimpleString(pid);
//        String filename = String.format("%s%s.json", "record", pid);
//        return Optional.of(filename);
//    }
//
//    private String pidToSimpleString(String pid) {
//        pid = pid.replace('/', '_');
//        pid = pid.replace('\\', '_');
//        pid = pid.replace('|', '_');
//        pid = pid.replace('.', '_');
//        pid = pid.replace(':', '_');
//        pid = pid.replace(',', '_');
//        pid = pid.replace('%', '_');
//        pid = pid.replace('!', '_');
//        pid = pid.replace('$', '_');
//        return pid;
//    }
//
  /**
   * Upload a document to elasticsearch.
   *
   * @param jsonDocument JSON document
   * @param index index of the document (one index per schema)
   * @param document_id id of the document
   * @return
   */
  public boolean uploadToElastic(String jsonDocument, String index, String document_id) {
    String ingestUrl = String.format("%s/%s/_doc/{id}", baseUrl, index);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<String>(jsonDocument, headers);
    try {
      ResponseEntity<String> response = restTemplate.exchange(ingestUrl,
              HttpMethod.PUT,
              entity,
              String.class,
              urlEncode(document_id));
    } catch (Exception e) {
      LOGGER.error("Could not send to url", e);
      return false;
    }
    return true;
  }

  public ResponseEntity<String> getFromElastic(String index, String document_id) {
    String accessUrl = String.format("%s/%s/_doc/{id}", baseUrl, index);
    ResponseEntity<String> entity = restTemplate.getForEntity(accessUrl,
            String.class,
            urlEncode(document_id));
    LOGGER.trace("Status code value: " + entity.getStatusCodeValue());
    LOGGER.trace("HTTP Header 'ContentType': " + entity.getHeaders().getContentType());
    return entity;
  }

  public String getDocumentFromResponse(ResponseEntity<String> response) {
    String jsonDocument = null;
    if (response.getStatusCode() == HttpStatus.OK) {
      System.out.println("++++" + response.getBody() + "++++++");
      JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);
      
      jsonDocument = jsonObject.getAsJsonObject("_source").toString();
    }
    return jsonDocument;
  }

  private String urlEncode(String forbiddenString) {
    String encodedString = forbiddenString;
    try {
      encodedString = URLEncoder.encode(forbiddenString, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException ex) {
      LOGGER.error(null, ex);
    }
    return encodedString;
  }

  private String urlDecode(String urlEncodedString) {
    String decodedString = urlEncodedString;
    try {
      decodedString = URLDecoder.decode(urlEncodedString, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException ex) {
      LOGGER.error(null, ex);
    }
    return decodedString;
  }
}
