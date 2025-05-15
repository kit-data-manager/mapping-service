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
package edu.kit.datamanager.mappingservice.impl;

import edu.kit.datamanager.exceptions.BadArgumentException;
import edu.kit.datamanager.mappingservice.configuration.ApplicationProperties;
import edu.kit.datamanager.mappingservice.dao.IMappingRecordDao;
import edu.kit.datamanager.mappingservice.domain.JobStatus;
import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import edu.kit.datamanager.mappingservice.exception.DuplicateMappingException;
import edu.kit.datamanager.mappingservice.exception.JobNotFoundException;
import edu.kit.datamanager.mappingservice.exception.JobProcessingException;
import edu.kit.datamanager.mappingservice.exception.MappingJobException;
import edu.kit.datamanager.mappingservice.exception.MappingNotFoundException;
import edu.kit.datamanager.mappingservice.exception.MappingServiceException;
import edu.kit.datamanager.mappingservice.exception.MappingServiceUserException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginException;
import edu.kit.datamanager.mappingservice.plugins.MappingPluginState;
import edu.kit.datamanager.mappingservice.plugins.PluginManager;
import edu.kit.datamanager.mappingservice.util.FileUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.scheduling.annotation.Async;

/**
 * Service for managing mappings.
 */
@Service
public class MappingService {

    protected static final String JOB_WITH_SUPPLIED_JOB_ID_NOT_FOUND = "Job with supplied job-id not found!";

    /**
     * Repo holding all MappingRecords.
     */
    @Autowired
    private IMappingRecordDao mappingRepo;

    /**
     * The Plugin Manager.
     */
    @Autowired
    private PluginManager pluginManager;

    @Autowired
    protected JobManager jobManager;

    /**
     * Path to directory holding all mapping files.
     */
    private Path mappingsDirectory;

    /**
     * Path to directory holding all job outputs.
     */
    private Path jobsOutputDirectory;

    private ApplicationProperties applicationProperties;
    private final MeterRegistry meterRegistry;

    /**
     * Logger for this class.
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(MappingService.class);

    @Autowired
    public MappingService(ApplicationProperties applicationProperties, MeterRegistry meterRegistry) {
        this.applicationProperties = applicationProperties;
        this.meterRegistry = meterRegistry;
        init(this.applicationProperties);
    }

    /**
     * Save content to mapping file and get the mapping location.
     *
     * @param content Content of the mapping file.
     * @param mappingRecord record of the mapping
     *
     * @return The created mapping record.
     *
     * @throws IOException if the provided content could not be persisted.
     */
    public MappingRecord createMapping(String content, MappingRecord mappingRecord) throws IOException {
        LOGGER.trace("Creating mapping with id {}.", mappingRecord.getMappingId());
        // Check for valid mapping ID (should contain at least one non whitespace.
        String mappingId = mappingRecord.getMappingId();
        if ((mappingId == null) || (mappingId.isBlank())) {
            String message = String.format("MappingID shouldn't be empty or contain only whitespaces. You provide '%s'", mappingId);
            LOGGER.error(message);
            throw new BadArgumentException(message);
        }

        String mappingType = mappingRecord.getMappingType();
        if ((mappingType == null) || (mappingType.isBlank() || !pluginManager.getPlugins().containsKey(mappingType))) {
            String message = String.format("MappingType shouldn't be empty or contain only whitespaces and must be a registered plugin id. You provide '%s'", mappingType);
            LOGGER.error(message);
            throw new BadArgumentException(message);
        }

        Iterable<MappingRecord> findMapping = mappingRepo.findByMappingIdIn(Collections.singletonList(mappingRecord.getMappingId()));
        if (findMapping.iterator().hasNext()) {
            LOGGER.error("Unable to create mapping with id {}. Mapping id is alreadyy used.", mappingRecord.getMappingId());
            mappingRecord = findMapping.iterator().next();
            throw new DuplicateMappingException("Error: Mapping '" + mappingRecord.getMappingType() + "_" + mappingRecord.getMappingId() + "' already exists!");
        }

        LOGGER.trace("Saving mapping file.");
        saveMappingFile(content, mappingRecord);
        LOGGER.trace("Persisting mapping record.");
        MappingRecord result = mappingRepo.save(mappingRecord);
        LOGGER.trace("Mapping with id {} successfully created.", result.getMappingId());
        return mappingRecord;
    }

    /**
     * Update content of mapping file and get the mapping location.
     *
     * @param content Content of the mapping file.
     * @param mappingRecord record of the mapping
     */
    public void updateMapping(String content, MappingRecord mappingRecord) throws MappingNotFoundException, IOException {
        Optional<MappingRecord> findMapping = mappingRepo.findByMappingId(mappingRecord.getMappingId());
        if (findMapping.isEmpty()) {
            LOGGER.error("Failed to update mapping with id {}. Mapping not found.", mappingRecord.getMappingId());
            throw new MappingNotFoundException("Error: Mapping '" + mappingRecord.getMappingType() + "_" + mappingRecord.getMappingId() + "' doesn't exist!");
        }

        LOGGER.trace("Updating mapping with id {}.", mappingRecord.getMappingId());
        mappingRecord.setMappingDocumentUri(findMapping.get().getMappingDocumentUri());
        LOGGER.trace("Saving mapping file.");
        saveMappingFile(content, mappingRecord);
        LOGGER.trace("Persisting mapping record.");
        mappingRepo.save(mappingRecord);
        LOGGER.trace("Mapping with id {} successfully updated.", mappingRecord.getMappingId());
    }

    /**
     * Delete mapping file and its record.
     *
     * @param mappingRecord record of the mapping
     */
    public void deleteMapping(MappingRecord mappingRecord) {
        Optional<MappingRecord> findMapping = mappingRepo.findByMappingId(mappingRecord.getMappingId());
        if (findMapping.isEmpty()) {
            //deletion skipped, no error needed
            LOGGER.trace("Mapping with id {} not found. Skipping deletion.", mappingRecord.getMappingId());
            return;
        }
        LOGGER.trace("Deleting mapping with id {}.", mappingRecord.getMappingId());
        mappingRecord = findMapping.get();
        mappingRepo.delete(mappingRecord);
        LOGGER.trace("Mapping with id {} deleted.", mappingRecord.getMappingId());
        try {
            deleteMappingFile(mappingRecord);
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to delete mapping file at %s. Please remove it manually.", mappingRecord.getMappingDocumentUri()), e);
        }
    }

    /**
     * Execute mapping and get the location of result file. If no according
     * mapping is found the src file will be returned.
     *
     * @param contentUrl Content of the src file.
     * @param mappingId id of the mapping
     * @return Path to result file.
     */
    public Optional<Path> executeMapping(URI contentUrl, String mappingId) throws MappingPluginException {
        LOGGER.trace("Executing mapping of content {} using mapping with id {}.", contentUrl, mappingId);
        if (contentUrl == null || mappingId == null) {
            throw new MappingPluginException(MappingPluginState.INVALID_INPUT(), "Either contentUrl or mappingId are not provided.");
        }

        Optional<Path> returnValue;
        Path srcFile = Paths.get(contentUrl);
        MappingRecord mappingRecord;

        LOGGER.trace("Searching for mapping with id {}.", mappingId);
        Optional<MappingRecord> optionalMappingRecord = mappingRepo.findByMappingId(mappingId);
        if (optionalMappingRecord.isPresent()) {
            LOGGER.trace("Mapping for id {} found.", mappingId);
            mappingRecord = optionalMappingRecord.get();

            Counter.builder("mapping_service.plugin_usage").tag("plugin", mappingRecord.getMappingType()).register(meterRegistry).increment();

            Path mappingFile = Paths.get(mappingRecord.getMappingDocumentUri());
            // execute mapping
            Path resultFile;
            LOGGER.trace("Preparing temporary output file.");
            resultFile = FileUtil.createTempFile(mappingId + "_" + srcFile.hashCode(), ".result");
            LOGGER.trace("Temporary output file available at {}. Performing mapping.", resultFile);
            MappingPluginState result = pluginManager.mapFile(mappingRecord.getMappingType(), mappingFile, srcFile, resultFile);
            LOGGER.trace("Mapping returned with result {}. Returning result file.", result);
            returnValue = Optional.of(resultFile);
            // remove downloaded file
            LOGGER.trace("Removing user upload.");
            FileUtil.removeFile(srcFile);
            LOGGER.trace("User upload successfully removed.");
        } else {
            LOGGER.error("Unable to find mapping for id {}.", mappingId);
            throw new MappingNotFoundException(String.format("Unable to find mapping with id %s.", mappingId));
        }
        return returnValue;
    }

    /**
     * Schedule an asynchronous job execution. The job will be scheduled and can
     * be monitored. As soon as the job has finished successfully, the output
     * can be downloaded or the job can be deleted.
     *
     * @param jobId The job's id.
     * @param contentUrl The URL of the user upload.
     * @param mappingId The id of the mapping to be used.
     *
     * @return Job status as completable future.
     *
     * @throws MappingPluginException if calling the plugin fails.
     */
    @Async("asyncExecutor")
    public CompletableFuture<JobStatus> executeMappingAsync(String jobId, URI contentUrl, String mappingId) throws MappingPluginException {
        LOGGER.trace("Executing mapping of content {} using mapping with id {}.", contentUrl, mappingId);
        CompletableFuture<JobStatus> task = new CompletableFuture<>();

        if (contentUrl == null || mappingId == null) {
            task.complete(JobStatus.error(jobId, JobStatus.STATUS.FAILED, "Either contentUrl or mappingId are not provided."));
        } else {
            Optional<Path> returnValue;
            Path srcFile = Paths.get(contentUrl);
            MappingRecord mappingRecord;

            // Get mapping file
            LOGGER.trace("Searching for mapping with id {}.", mappingId);
            Optional<MappingRecord> optionalMappingRecord = mappingRepo.findByMappingId(mappingId);
            if (optionalMappingRecord.isPresent()) {
                LOGGER.trace("Mapping for id {} found. Creating temporary output file.", mappingId);
                mappingRecord = optionalMappingRecord.get();
                Path mappingFile = Paths.get(mappingRecord.getMappingDocumentUri());
                // execute mapping
                Path resultFile = getOutputFile(jobId).toPath();
                LOGGER.trace("Temporary output file available at {}. Performing mapping.", resultFile);
                try {
                    MappingPluginState result = pluginManager.mapFile(mappingRecord.getMappingType(), mappingFile, srcFile, resultFile);

                    LOGGER.trace("Mapping returned with result state {}. Returning result file.", result.getState());
                    returnValue = Optional.of(resultFile);
                    LOGGER.trace("Fixing file extension for output {}", returnValue.get());
                    Path outputPath = FileUtil.fixFileExtension(returnValue.get());
                    LOGGER.trace("Fixed output path: {}", outputPath);

                    task.complete(JobStatus.complete(jobId, JobStatus.STATUS.SUCCEEDED, outputPath.toFile()));
                } catch (MappingPluginException t) {
                    LOGGER.error("Asynchronous job execution failed with error.", t);
                    task.complete(JobStatus.error(jobId, JobStatus.STATUS.FAILED, t.getMessage()));
                } finally {
                    // remove downloaded file
                    LOGGER.trace("Removing user upload at {}.", srcFile);
                    FileUtil.removeFile(srcFile);
                    LOGGER.trace("User upload successfully removed.");
                }
            } else {
                LOGGER.error("Unable to find mapping with id {}.", mappingId);
                task.complete(JobStatus.error(jobId, JobStatus.STATUS.FAILED, "Unable to find mapping with id " + mappingId + "."));
                //throw new MappingNotFoundException("Unable to find mapping with id " + mappingId + ".");
            }
        }
        return task;
    }

    /**
     * Fetch a job's status or fail if the job cannot be found.
     *
     * @param jobId The job's id.
     *
     * @return The job status as completable future.
     *
     * @throws JobNotFoundException If no job for the provided jobId exists.
     */
    public CompletableFuture<JobStatus> fetchJobElseThrowException(String jobId) throws JobNotFoundException {
        CompletableFuture<JobStatus> job = fetchJob(jobId);
        if (null == job) {
            LOGGER.error("Job with id {} not found.", jobId);
            throw new JobNotFoundException(JOB_WITH_SUPPLIED_JOB_ID_NOT_FOUND);
        }
        return job;
    }

    public CompletableFuture<JobStatus> fetchJob(String jobId) {
        @SuppressWarnings("unchecked")
        CompletableFuture<JobStatus> completableFuture = (CompletableFuture<JobStatus>) jobManager.getJob(jobId);

        return completableFuture;
    }

    /**
     * Query a job's status.
     *
     * @param jobId The job's id.
     *
     * @return The Job status.
     *
     * @throws Throwable Any kind of error produced during job execution.
     */
    public JobStatus getJobStatus(String jobId) throws Throwable {
        CompletableFuture<JobStatus> completableFuture = fetchJobElseThrowException(jobId);

        if (!completableFuture.isDone()) {
            return JobStatus.status(jobId, JobStatus.STATUS.RUNNING);
        }

        Throwable[] errors = new Throwable[1];
        JobStatus[] simpleResponses = new JobStatus[1];
        completableFuture.whenComplete((response, ex) -> {
            if (ex != null) {
                errors[0] = ex.getCause();
            } else {
                response.setOutputFileURI(String.format("/api/v1/mappingExecution/schedule/%s/download", jobId));
                simpleResponses[0] = response;
            }
        });

        if (errors[0] != null) {
            if (errors[0] instanceof MappingPluginException mappingPluginException) {
                mappingPluginException.throwMe();
            }
            throw errors[0];
        }

        return simpleResponses[0];
    }

    /**
     * Get the job's output file.
     *
     * @param jobId The jobId.
     *
     * @return The local file.
     *
     * @throws JobNotFoundException If no output file for the provided jobId
     * could be found.
     * @throws JobProcessingException If the job has not finished, yet.
     * @throws Throwable Any kind of error produced during job execution.
     */
    public File getJobOutputFile(String jobId) throws Throwable {
        CompletableFuture<JobStatus> completableFuture = fetchJob(jobId);

        if (null == completableFuture) {
            //return output file in case it still exists (even after the job was removed from the queue)
            File outputFile = getOutputFile(jobId);
            if (outputFile.exists()) {
                return outputFile;
            }
            //nothing left, return error
            throw new JobNotFoundException(JOB_WITH_SUPPLIED_JOB_ID_NOT_FOUND);
        }

        if (!completableFuture.isDone()) {
            LOGGER.trace("Job {} not finished, yet. Returning RUNNING state.", jobId);
            throw new JobProcessingException("Job is still in progress...", true);
        }

        LOGGER.trace("Obtaining output from job status.");
        Throwable[] errors = new Throwable[1];
        JobStatus[] jobStatus = new JobStatus[1];
        completableFuture.whenComplete((response, ex) -> {
            if (ex != null) {
                errors[0] = ex.getCause();
            } else {
                jobStatus[0] = response;
            }
        });

        if (errors[0] != null) {
            if (errors[0] instanceof MappingPluginException mappingPluginException) {
                mappingPluginException.throwMe();
            }
            throw errors[0];
        }

        return jobStatus[0].getJobOutput();
    }

    /**
     * Delete the job with the provided jobId and all associated data. If the
     * job is no longer managed, only the data is removed. If the job is still
     * managed and not running, the removal is scheduled. If the job is still
     * running, an according status is returned.
     *
     * @param jobId The id of the job.
     *
     * @return The status of the job, either with status DELETED or RUNNING.
     */
    public JobStatus deleteJobAndAssociatedData(String jobId) {
        CompletableFuture<JobStatus> completableFuture = fetchJob(jobId);

        if (null == completableFuture) {
            //delete output file if file still exists (even after the job was removed from the queue)
            File outputFile = getOutputFile(jobId);
            if (outputFile.exists()) {
                outputFile.delete();
            } else {
                LOGGER.debug("No output file for job {} found.", jobId);
            }
            return JobStatus.status(jobId, JobStatus.STATUS.DELETED);
        }

        if (!completableFuture.isDone()) {
            LOGGER.trace("Job {} not finished, yet. Returning RUNNING state.", jobId);
            return JobStatus.status(jobId, JobStatus.STATUS.RUNNING);
        }

        LOGGER.trace("Cleaning up all job artifacts");
        completableFuture.whenComplete((response, ex) -> {
            if (ex != null) {
                LOGGER.error("Job failed with exception.", ex);
            }
            final File jobOutput;
            if (null != response && null != response.getJobOutput()) {
                jobOutput = response.getJobOutput();
            } else {
                jobOutput = getOutputFile(jobId);
            }

            if (jobOutput.exists()) {
                LOGGER.trace(" - Deleting job output");
                jobOutput.delete();
            }
            LOGGER.trace("Removing job from queue.");
            jobManager.removeJob(jobId);
        });

        return JobStatus.status(jobId, JobStatus.STATUS.DELETED);
    }

    /**
     * Get the job's output file.
     *
     * @param jobId The jobId.
     *
     * @return File A local file.
     */
    private File getOutputFile(String jobId) {
        Matcher m = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$").matcher(jobId);
        if (!m.matches()) {
            throw new MappingJobException(String.format("Invalid jobId %s provided.", jobId));
        }

        Path outputPath = jobsOutputDirectory.resolve(String.format("%s.out", jobId)).normalize();
        if (!outputPath.startsWith(jobsOutputDirectory)) {
            throw new IllegalArgumentException(String.format("Invalid jobId %s provided.", jobId));
        }
        return outputPath.toFile();
    }

    /**
     * Initalize mappings directory and mappingUtil instance.
     *
     * @param applicationProperties Properties holding mapping directory
     * setting.
     */
    private void init(ApplicationProperties applicationProperties) {
        if ((applicationProperties != null) && (applicationProperties.getMappingsLocation() != null)) {
            try {
                mappingsDirectory = Files.createDirectories(new File(applicationProperties.getMappingsLocation().getPath()).getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new MappingServiceException(String.format("Could not initialize mappings directory '%s' for mapping.", applicationProperties.getMappingsLocation()), e);
            }
            try {
                jobsOutputDirectory = Files.createDirectories(new File(applicationProperties.getJobOutputLocation().getPath()).getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new MappingServiceException(String.format("Could not initialize job output directory '%s'.", applicationProperties.getJobOutputLocation()), e);
            }
            try {
                Files.createDirectories(new File(applicationProperties.getCodeLocation().getPath()).getAbsoluteFile().toPath());
            } catch (IOException e) {
                throw new MappingServiceException(String.format("Could not initialize code target directory '%s'.", applicationProperties.getCodeLocation()), e);
            }
        } else {
            throw new MappingServiceException("Cannot configure MappingService due to missing application.properties.");
        }
    }

    /**
     * Save mapping file to mapping directory.
     *
     * @param content Content of file.
     * @param mapping record of mapping
     * @throws IOException error writing file.
     */
    private void saveMappingFile(String content, MappingRecord mapping) throws IOException {
        Path newMappingFile;
        if ((content != null) && (mapping != null) && (mapping.getMappingId() != null) && (mapping.getMappingType() != null)) {
            LOGGER.debug("Storing mapping file with id '{}' and type '{}'", mapping.getMappingId(), mapping.getMappingType());
            LOGGER.trace("Content of mapping: '{}'", content);
            try {
                // 'delete' old file
                deleteMappingFile(mapping);
                newMappingFile = Paths.get(mappingsDirectory.toString(), mapping.getMappingId() + "_" + mapping.getMappingType() + ".mapping");
                LOGGER.trace("Write content to '{}'", newMappingFile);
                FileUtils.writeStringToFile(newMappingFile.toFile(), content, StandardCharsets.UTF_8);
                mapping.setMappingDocumentUri(newMappingFile.toString());
                byte[] data = content.getBytes();

                MessageDigest md = MessageDigest.getInstance("SHA256");
                md.update(data, 0, data.length);

                mapping.setDocumentHash("sha256:" + Hex.encodeHexString(md.digest()));
            } catch (NoSuchAlgorithmException ex) {
                String message = "Failed to initialize SHA256 MessageDigest.";
                LOGGER.error(message, ex);
                throw new MappingServiceException(message, ex);
            }
        } else {
            throw new MappingServiceUserException("Failed to update mapping file. No content or mapping record provided.");
        }
    }

    /**
     * Delete mapping file. The file will not be deleted. Add actual datetime as
     * suffix
     *
     * @param mapping record of mapping
     * @throws IOException error writing file.
     */
    private void deleteMappingFile(MappingRecord mapping) throws IOException {
        if ((mapping != null) && (mapping.getMappingDocumentUri() != null)) {
            LOGGER.debug("Delete mapping file '{}'", mapping.getMappingDocumentUri());
            Path deleteFile = Paths.get(mapping.getMappingDocumentUri());
            if (deleteFile.toFile().exists()) {
                Path newFileName = Paths.get(deleteFile.getParent().toString(), deleteFile.getFileName() + date2String());
                LOGGER.trace("Move mapping file fo '{}'", newFileName);
                FileUtils.moveFile(deleteFile.toFile(), newFileName.toFile());
            }
        }
    }

    /**
     * Create string from actual date.
     *
     * @return formatted date.
     */
    private String date2String() {
        SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss");
        return sdf.format(new Date());
    }
}
