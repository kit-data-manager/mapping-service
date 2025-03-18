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
import edu.kit.datamanager.mappingservice.domain.JobStatus;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.exception.JobProcessingException;
import edu.kit.datamanager.mappingservice.exception.MappingException;
import edu.kit.datamanager.mappingservice.exception.MappingExecutionException;
import edu.kit.datamanager.mappingservice.exception.MappingJobException;
import edu.kit.datamanager.mappingservice.exception.MappingNotFoundException;
import edu.kit.datamanager.mappingservice.impl.JobManager;
import edu.kit.datamanager.mappingservice.impl.MappingService;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.rest.IMappingExecutionController;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

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
    protected JobManager jobManager;
    private final IMappingRecordDao mappingRecordDao;
    private final MeterRegistry meterRegistry;
    private final DistributionSummary documentsInSizeMetric;
    private final DistributionSummary documentsOutSizeMetric;

    public MappingExecutionController(MappingService mappingService, IMappingRecordDao mappingRecordDao, JobManager jobManager, MeterRegistry meterRegistry) {
        this.mappingService = mappingService;
        this.mappingRecordDao = mappingRecordDao;
        this.jobManager = jobManager;
        this.meterRegistry = meterRegistry;
        this.documentsInSizeMetric = DistributionSummary.builder("mapping_service.documents.input_size").baseUnit("bytes").register(meterRegistry);
        this.documentsOutSizeMetric = DistributionSummary.builder("mapping_service.documents.output_size").baseUnit("bytes").register(meterRegistry);
    }

    @Override
    public void mapDocument(MultipartFile document, String mappingID, HttpServletRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder) {
        LOG.trace("Performing mapDocument(File#{}, {})", document.getOriginalFilename(), mappingID);

        Optional<Path> resultPath;
        if (!document.isEmpty() && !mappingID.isBlank()) {
            LOG.trace("Obtaining mapping for id {}.", mappingID);
            Optional<MappingRecord> record = mappingRecordDao.findByMappingId(mappingID);
            if (record.isEmpty()) {
                String message = String.format("No mapping found for mapping id %s.", mappingID);
                LOG.error(message + " Returning HTTP 404.");
                throw new MappingNotFoundException(message);
                //return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            }

            LOG.trace("Receiving mapping input file.");
            String extension = "." + FilenameUtils.getExtension(document.getOriginalFilename());
            LOG.trace("Found file extension: {}", extension);
            Path inputPath = FileUtil.createTempFile("inputMultipart", extension);
            LOG.trace("Writing user upload to: {}", inputPath);
            File inputFile = inputPath.toFile();
            try {
                document.transferTo(inputFile);
                LOG.trace("Successfully received user upload.");
            } catch (IOException e) {
                LOG.error("Failed to receive upload from user.", e);
                throw new MappingExecutionException("Unable to write user upload to disk.");
            }

            try {
                LOG.trace("Performing mapping process of file {} via mapping service", inputPath.toString());

                resultPath = mappingService.executeMapping(inputFile.toURI(), mappingID);
                if (resultPath.isPresent()) {
                    LOG.trace("Mapping process finished. Output written to {}.", resultPath.toString());
                } else {
                    throw new MappingPluginException(MappingPluginState.UNKNOWN_ERROR, "Mapping process finished, but no result was returned.");
                }
            } catch (MappingPluginException e) {
                LOG.error("Failed to execute mapping.", e);
                throw new MappingExecutionException("Failed to execute mapping with id " + mappingID + " on provided input document.");
            } finally {
                LOG.trace("Removing user upload at {}.", inputFile);
                FileUtil.removeFile(inputPath);
                LOG.trace("User upload successfully removed.");
            }
        } else {
            String message = "Either mapping id or input document are missing. Unable to perform mapping.";
            LOG.error(message);
            throw new MappingException(message);
        }
        Path result = resultPath.get();
        if (!Files.exists(result) || !Files.isRegularFile(result) || !Files.isReadable(result)) {
            String message = "The mapping result expected at path " + result + " is not accessible. This indicates an error of the mapper implementation.";
            LOG.error(message);
            throw new MappingExecutionException(message);
        }

        LOG.trace("Determining mime type for mapping result.");
        result = FileUtil.fixFileExtension(result);

        String mimeType = FileUtil.getMimeType(result);
        LOG.trace("Determining file extension for mapping result.");
        String extension = FileUtil.getExtensionForMimeType(mimeType);

        LOG.trace("Using mime type {} and extension {}.", mimeType, extension);

        response.setStatus(HttpStatus.OK.value());
        response.setHeader("Content-Type", mimeType);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(result.toFile().length()));
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;" + "filename=result" + extension);
        try {
            Files.copy(result, response.getOutputStream());

        } catch (IOException ex) {
            String message = "Failed to write mapping result file to stream.";
            LOG.error(message, ex);
            throw new MappingExecutionException(message);
        } finally {
            Counter.builder("mapping_service.mapping_usage").tag("mappingID", mappingID).register(meterRegistry).increment();
            this.documentsInSizeMetric.record(document.getSize());
            this.documentsOutSizeMetric.record(result.toFile().length());

            LOG.trace("Result file successfully transferred to client. Removing file {} from disk.", result);
            try {
                Files.delete(result);
                LOG.trace("Result file successfully removed.");
            } catch (IOException ignored) {
                LOG.warn("Failed to remove result file. Please remove manually.");
            }
        }
    }

    @Override
    public ResponseEntity<JobStatus> scheduleMapDocument(String mappingID, MultipartFile document, HttpServletRequest request, HttpServletResponse response, UriComponentsBuilder uriBuilder) throws Throwable {
        LOG.trace("Performing mapDocument(File#{}, {})", document.getOriginalFilename(), mappingID);
        String jobId = UUID.randomUUID().toString();

        if (null != jobManager.getJob(jobId)) {
            throw new JobProcessingException("JobId conflict, please retry again.", true);
        }

        LOG.info("Generated job-id {} for this request.", jobId);
        if (!document.isEmpty() && !mappingID.isBlank()) {
            LOG.trace("Obtaining mapping for id {}.", mappingID);
            Optional<MappingRecord> record = mappingRecordDao.findByMappingId(mappingID);
            if (record.isEmpty()) {
                String message = String.format("No mapping found for mapping id %s.", mappingID);
                LOG.error(message + " Returning HTTP 404.");
                throw new MappingNotFoundException(message);
                //return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            }

            LOG.trace("Receiving mapping input file.");
            String extension = "." + FilenameUtils.getExtension(document.getOriginalFilename());
            LOG.trace("Found file extension: {}", extension);
            Path inputPath = FileUtil.createTempFile("inputMultipart", extension);
            LOG.trace("Writing user upload to: {}", inputPath);
            File inputFile = inputPath.toFile();
            try {
                document.transferTo(inputFile);
                LOG.trace("Successfully received user upload.");
            } catch (IOException e) {
                LOG.error("Failed to receive upload from user.", e);
                throw new JobProcessingException("Unable to write user upload to disk.");
            }

            try {
                LOG.trace("Scheduling mapping process of file {} via mapping service", inputPath.toString());
                CompletableFuture<JobStatus> completableFuture = mappingService.executeMappingAsync(jobId, inputFile.toURI(), mappingID);
                jobManager.putJob(jobId, completableFuture);
                LOG.info("Job-id {} submitted for processing. Returning from controller.", jobId);
                return ResponseEntity.ok(JobStatus.status(jobId, JobStatus.STATUS.SUBMITTED));
            } catch (MappingPluginException e) {
                LOG.error("Failed to execute mapping.", e);
                LOG.trace("Removing user upload at {}.", inputFile);
                FileUtil.removeFile(inputPath);
                LOG.trace("User upload successfully removed.");
                return ResponseEntity.status(500).body(JobStatus.error(jobId, JobStatus.STATUS.FAILED, "Failed to execute mapping with id " + mappingID + " on provided input document."));
            }
            /*finally {
                LOG.trace("Removing user upload at {}.", inputFile);
                FileUtil.removeFile(inputPath);
                LOG.trace("User upload successfully removed.");
            }*/
        } else {
            String message = "Either mapping id or input document are missing. Unable to perform mapping.";
            LOG.error(message);
            throw new JobProcessingException(message);
        }
    }

    @Override
    public ResponseEntity<JobStatus> getJobStatus(@PathVariable(name = "job-id") String jobId) throws Throwable {
        LOG.debug("Received request to fetch status of job-id: {}", jobId);

        JobStatus status = mappingService.getJobStatus(jobId);
        return ResponseEntity.ok(status);
    }

    @Override
    public ResponseEntity<Resource> getJobOutputFile(@PathVariable(name = "job-id") String jobId) throws Throwable {
        LOG.debug("Received request to fetch output file of job-id: {}", jobId);

        File outputFile = mappingService.getJobOutputFile(jobId);

        String mimeType = FileUtil.getMimeType(outputFile.toPath());
        LOG.trace("Determining file extension for mapping result.");
        String extension = FileUtil.getExtensionForMimeType(mimeType);

        LOG.trace("Using mime type {} and extension {}.", mimeType, extension);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(outputFile));

        return ResponseEntity.ok()
                .contentLength(outputFile.length())
                .contentType(MediaType.parseMediaType(mimeType))
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;" + "filename=result" + extension)
                .body(resource);
    }

    @Override
    public ResponseEntity deleteJobAndAssociatedData(@PathVariable(name = "job-id") String jobId) throws Throwable {
        LOG.debug("Received request to delete job-id: {}", jobId);

        JobStatus status = mappingService.deleteJobAndAssociatedData(jobId);
        if (status.getStatus().equals(JobStatus.STATUS.DELETED)) {
            LOG.debug("Job removal result: {}", status);
        } else {
            LOG.debug("Job could not be deleted as it is not finished, yet.");
            throw new MappingJobException("Job not finished, yet.");
        }

        return ResponseEntity.noContent().build();
    }
}
