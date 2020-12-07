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

import edu.kit.datamanager.indexer.configuration.ApplicationProperties;
import edu.kit.datamanager.indexer.exception.IndexerException;
import edu.kit.datamanager.indexer.mapping.MappingUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
  /**
   * Path to directory holding all mapping files.
   */
  private Path mappingsDirectory;

  /**
   * MappingUtil for executing mappings.
   */
  private MappingUtil mappingUtil;
  
  private RestTemplate restTemplate = new RestTemplate();

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
    if ((applicationProperties != null) && (applicationProperties.getMappingsLocation() != null)) {
      mappingUtil = new MappingUtil(applicationProperties);
      try {
        mappingsDirectory = Files.createDirectories(Paths.get(applicationProperties.getMappingsLocation())).toAbsolutePath();
      } catch (IOException e) {
        throw new IndexerException("Could not initialize directory '" + applicationProperties.getMappingsLocation() + "' for mapping.", e);
      }
    } else {
      throw new IndexerException("Could not initialize mapping directory due to missing location!");
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
   * @param jsonDocument JSON document 
   * @param index index of the document (one index per schema)
   * @param document_id id of the document
   * @return 
   */
    private boolean uploadToElastic(String jsonDocument, String index, String document_id) {
        String baseUrl = applicationProperties.getElasticsearchUrl();
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
//  RestTemplate<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.PUT, new HttpEntity<>(jsonDocument, headers), String.class);
//    collectResponseHeaders(response.getHeaders());
//    LOGGER.trace("Request returned with status {}. Returning response body.", response.getStatusCodeValue());
//    return response.getBody();
//          SimpleServiceClient ssc = SimpleServiceClient.create(ingestUrl);
//          ssc.
//            URL elasticURL = new URL(ingestUrl);
//            HttpRequest request = HttpRequest.Builder()
//                .uri(elasticURL.toURI())
//                .header("Content-Type", "application/json")
//                .PUT(HttpRequest.BodyPublishers.ofString(json))
//                .build();
//            HttpClient client =  HttpClient();
//            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
////            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            return Optional.of("todo"); //response.body());
       } catch (Exception e) {
            LOGGER.error("Could not send to url", e);
            return false;
        }
       return true;
    }
     public ResponseEntity<String> getFromElastic(String index, String document_id) {
        String baseUrl = applicationProperties.getElasticsearchUrl();
        String accessUrl = String.format("%s/%s/_doc/{id}", baseUrl, index);
      ResponseEntity<String> entity = restTemplate.getForEntity(accessUrl,
                                                                  String.class,
                                                                  urlEncode(document_id));
      LOGGER.info("Status code value: " + entity.getStatusCodeValue());
      LOGGER.info("HTTP Header 'ContentType': " + entity.getHeaders().getContentType());
      return entity;
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
    public static void main(String[] args) {
    ApplicationProperties ap = new ApplicationProperties();
    ap.setElasticsearchUrl("http://localhost:9200");
    ap.setMappingsLocation("/tmp/test");
    IndexingService is = new IndexingService(ap);
    is.uploadToElastic("{\"name\" : \"volker\" }", "test", "volker hartmann");
    ResponseEntity<String> fromElastic = is.getFromElastic("test", "volker hartmann");
      System.out.println(fromElastic.getStatusCodeValue() + " --> " + fromElastic.getStatusCode().toString());
      System.out.println(fromElastic.getBody());
  }
}
