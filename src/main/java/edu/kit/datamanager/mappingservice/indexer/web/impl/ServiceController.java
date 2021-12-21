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
package edu.kit.datamanager.mappingservice.indexer.web.impl;

import edu.kit.datamanager.mappingservice.indexer.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.indexer.exception.IndexerException;
import edu.kit.datamanager.mappingservice.indexer.service.impl.MappingService;
import edu.kit.datamanager.mappingservice.indexer.util.ElasticsearchUtil;
import edu.kit.datamanager.mappingservice.indexer.web.IServiceController;
import edu.kit.datamanager.messaging.client.handler.IMessageHandler;
import edu.kit.datamanager.messaging.client.util.MessageHandlerUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing mapping files.
 */
@Controller
@RequestMapping(value = "/api/v1/service")
public class ServiceController implements IServiceController {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);

  @Autowired
  private MappingService mappingService;

  @Override
  public ResponseEntity mapDocument(MultipartFile document,
                                            String mappingID,
                                            String mappingType,
                                            HttpServletRequest request,
                                            HttpServletResponse response,
                                            UriComponentsBuilder uriBuilder) throws URISyntaxException {
//    LOG.debug("Document: {}", document.getName());
//    LOG.debug("MappingID: {}", mappingID);
//    LOG.debug("MappingTape: {}", mappingType);
//
//    Path resultPath = null;
//    if (!document.isEmpty() && !mappingID.isBlank() && !mappingType.isBlank()){
//      URI documentPath;
//      try {
//        documentPath = document.getResource().getURI();
//        resultPath = mappingService.executeMapping(documentPath, mappingID, mappingType).get();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    } else if (!document.isEmpty() && !mappingID.isBlank()){
//      URI documentPath;
//      try {
//        documentPath = document.getResource().getURI();
//        resultPath = mappingService.executeMapping(documentPath, mappingID).get(0);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    } else {
//      LOG.error("The input does not meet the minimal requirements. Have a look in the debug logs for more detailed information.");
//      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The input does not meet the minimal requirements." +
//              "Please check if you provided all necessary information. This is documented in swagger '/swagger-ui/index.html'.");
//    }
//
//
//    if (resultPath == null || !Files.exists(resultPath) || !Files.isRegularFile(resultPath) || !Files.isReadable(resultPath)) {
//      LOG.trace("The result path {} is for some reason not reachable.", resultPath);
//      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error while accessing result");
//    }
//
//    return ResponseEntity.
//            ok().
//            header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resultPath.toFile().length())).
//            body(new FileSystemResource(resultPath.toFile()));
    return null;
  }
}
