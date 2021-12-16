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
import edu.kit.datamanager.mappingservice.indexer.web.IServiceController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;

/**
 * Controller for managing mapping files.
 */
@Controller
@RequestMapping(value = "/api/v1/service")
public class ServiceController implements IServiceController {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceController.class);

  @Override
  public ResponseEntity<MappingRecord> mapDocument(MultipartFile document, String mappingId, String mappingtype, HttpServletRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder) throws URISyntaxException {
    return null;
  }
}
