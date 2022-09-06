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
package edu.kit.datamanager.mappingservice.rest.impl;

import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.impl.MappingService;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import edu.kit.datamanager.mappingservice.rest.IMappingExecutionController;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Controller for executing document mappings
 */
@Controller
@RequestMapping(value = "/api/v1/mappingExecution")
public class MappingExecutionController implements IMappingExecutionController {

    private static final Logger LOG = LoggerFactory.getLogger(MappingExecutionController.class);

    @Autowired
    private MappingService mappingService;

    @Autowired
    private IMappingRecordDao mappingRecordDao;

    @Override
    public ResponseEntity mapDocument(MultipartFile document,
                                      String mappingID,
                                      String mappingType,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      UriComponentsBuilder uriBuilder) {
        LOG.debug("Document: {}", document.getName());
        LOG.debug("MappingID: {}", mappingID);
        LOG.debug("MappingTape: {}", mappingType);

        Path resultPath = null;
        if (!document.isEmpty() && !mappingID.isBlank() && !mappingType.isBlank()) {
            Path inputPath = FileUtil.createTempFile("inputMultipart", "");
            File inputFile = inputPath.toFile();
            try {
                document.transferTo(inputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Optional<MappingRecord> record = mappingRecordDao.findByMappingIdAndMappingType(mappingID, mappingType);
            if (!record.isPresent()) {
                String message = String.format("No mapping record found for mapping %s/%s.", mappingID, mappingType);
                LOG.error(message + " Returning 404.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            }

            try {
                System.out.println(inputPath);
                resultPath = mappingService.executeMapping(inputFile.toURI(), mappingID, mappingType).get();
            } catch (Exception e) {
                LOG.error("Could not get resultPath");
                e.printStackTrace();
            }
            FileUtil.removeFile(inputPath);
        } else {
            LOG.error("The input does not meet the minimal requirements. Have a look in the debug logs for more detailed information.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The input does not meet the minimal requirements." +
                    "Please check if you provided all necessary information. This is documented in swagger '/swagger-ui/index.html'.");
        }

        if (resultPath == null) {
            String message = "There is no result for the input. The input must be invalid.";
            LOG.trace(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (!Files.exists(resultPath) || !Files.isRegularFile(resultPath) || !Files.isReadable(resultPath)) {
            LOG.trace("The result path {} is for some reason not reachable.", resultPath);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error while accessing result");
        }

        return ResponseEntity.
                ok().
                header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resultPath.toFile().length())).
                body(new FileSystemResource(resultPath.toFile()));
    }
}
