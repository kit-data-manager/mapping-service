/*
 * Copyright 2022 Karlsruhe Institute of Technology.
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
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.rest.IMappingExecutionController;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Controller for executing document mappings via REST API.
 *
 * @author maximilianiKIT
 */
@Controller
@RequestMapping(value = "/api/v1/mappingExecution")
public class MappingExecutionController implements IMappingExecutionController {

    private static final Logger LOG = LoggerFactory.getLogger(MappingExecutionController.class);

    private final MappingService mappingService;

    private final IMappingRecordDao mappingRecordDao;

    public MappingExecutionController(MappingService mappingService, IMappingRecordDao mappingRecordDao) {
        this.mappingService = mappingService;
        this.mappingRecordDao = mappingRecordDao;
    }

    @Override
    public ResponseEntity mapDocument(MultipartFile document, String mappingID, HttpServletRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder) {
        LOG.debug("Document: {}", document.getName());
        LOG.debug("MappingID: {}", mappingID);

        Path resultPath;
        if (!document.isEmpty() && !mappingID.isBlank()) {
            String extension = "." + FilenameUtils.getExtension(document.getOriginalFilename());
            LOG.trace("Found file with ending: {}", extension);
            Path inputPath = FileUtil.createTempFile("inputMultipart", extension);
            LOG.info("Saved file to: {}", inputPath);
            File inputFile = inputPath.toFile();
            try {
                document.transferTo(inputFile);
            } catch (IOException e) {
                LOG.error("Failed to receive file from client.", e);
                return ResponseEntity.internalServerError().body("Unable to write file to disk.");
            }

            Optional<MappingRecord> record = mappingRecordDao.findByMappingId(mappingID);
            if (record.isEmpty()) {
                String message = String.format("No mapping record found for mapping %s.", mappingID);
                LOG.error(message + " Returning 404.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            }

            try {
                LOG.debug(inputPath.toString());
                resultPath = mappingService.executeMapping(inputFile.toURI(), mappingID).get();
            } catch (MappingPluginException e) {
                LOG.error("Failed to execute mapping.", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to execute mapping with id " + mappingID + " on provided input document.");
            }
            LOG.trace("Removing uploaded file from {}.", inputFile);
            FileUtil.removeFile(inputPath);
            LOG.trace("Input file successfully removed.");
        } else {
            String message = "Either mappingID or input document are missing. Unable to perform mapping.";
            LOG.error(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }

        if (resultPath == null) {
            String message = "No mapping result was produced. Probably, the input document could not be processed by the mapper with id " + mappingID;
            LOG.error(message);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (!Files.exists(resultPath) || !Files.isRegularFile(resultPath) || !Files.isReadable(resultPath)) {
            String message = "The mapping result expected at path "+ resultPath + "F is not accessible. This indicates an error of the mapper implementation.";
            LOG.trace(message);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
        }

        LOG.trace("Successfully mapped document with mapping {}.", mappingID);
        return ResponseEntity.ok().
                header(HttpHeaders.CONTENT_LENGTH, String.valueOf(resultPath.toFile().length())).
                header(HttpHeaders.CONTENT_TYPE, String.valueOf("application/zip")).
                header(HttpHeaders.CONTENT_DISPOSITION, String.valueOf("attachment; " + "result.zip")).
                body(new FileSystemResource(resultPath.toFile()));
    }
}
