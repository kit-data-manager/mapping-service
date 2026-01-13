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
package edu.kit.datamanager.mappingservice.rest;

import edu.kit.datamanager.mappingservice.domain.JobStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URISyntaxException;

/**
 * Interface and documentation for mapping execution REST-API.
 *
 * @author maximilianiKIT
 */
@ApiResponses(value = {
    @ApiResponse(responseCode = "401", description = "UNAUTHORIZED is returned if authorization in required but was not provided."),
    @ApiResponse(responseCode = "403", description = "FORBIDDEN is returned if the caller has no sufficient privileges.")})
public interface IMappingExecutionController {

    @Operation(summary = "Map a document with an existing mapping.", description = "This endpoint allows the mapping of documents via a file upload. "
            + "The prerequisite for this is a mapping that has already been created in advance via the \"/api/v1/mappingAdministration\" endpoint or the GUI. "
            + "The identifier of this mapping must then be passed to this endpoint as parameters together with the document to be mapped.", responses = {
                @ApiResponse(responseCode = "200", description = "OK is returned if the mapping was successful. "
                        + "The result will also be returned in the response."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no mapping for mappingID could be found."),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if a parameter is missing or the mapping could not be performed with the provided input. It is "
                        + "expected that a mapping plugin accepts a well defined input and produces results for proper inputs. Therefore, only a faulty input "
                        + "document should be the reason for a mapper to fail."),
                @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR is returned the mapping returned successfully, but the mapping result "
                        + "is not accessible. This is expected to be an error in the mapping implementation and should be fixed in there.")})

    @RequestMapping(value = {"/{mappingID}"}, method = {RequestMethod.POST}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    void mapDocument(
            @Parameter(description = "The document to be mapped.", required = true) @RequestPart(name = "document") final MultipartFile document,
            @Parameter(description = "The mappingID of the already defined mapping.", required = true) @PathVariable(value = "mappingID") String mappingID,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder) throws URISyntaxException;

    @Operation(summary = "Map a document with an existing mapping. The mapping is executed asynchronously, the call returns a JobStatus document "
            + "that contains the jobId used to query the execution status.", description = "This endpoint allows the asynchronous mapping of documents via a file upload. "
            + "The prerequisite for this is a mapping that has already been created in advance via the \"/api/v1/mappingAdministration\" endpoint or the GUI. "
            + "The identifier of this mapping must then be passed to this endpoint as parameters together with the document to be mapped.", responses = {
                @ApiResponse(responseCode = "200", description = "OK is returned if the mapping was successful. "
                        + "The result will also be returned in the response."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no mapping for mappingID could be found."),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if a parameter is missing or the mapping could not be performed with the provided input. It is "
                        + "expected that a mapping plugin accepts a well defined input and produces results for proper inputs. Therefore, only a faulty input "
                        + "document should be the reason for a mapper to fail."),
                @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR is returned the mapping returned successfully, but the mapping result "
                        + "is not accessible. This is expected to be an error in the mapping implementation and should be fixed in there.")})


    @Operation(summary = "Map a document directly using the provided plugin.", description = "This endpoint allows the mapping of documents via a file upload. "
            + "The identifier of the plugin must be passed to this endpoint as parameters together with the document to be mapped and the mapping rules.", responses = {
            @ApiResponse(responseCode = "200", description = "OK is returned if the mapping was successful. "
                    + "The result will also be returned in the response."),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no plugin for pluginId could be found."),
            @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if a parameter is missing or the mapping could not be performed with the provided input. It is "
                    + "expected that a mapping plugin accepts a well defined input and produces results for proper inputs. Therefore, only a faulty input "
                    + "document should be the reason for a mapper to fail."),
            @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR is returned the mapping returned successfully, but the mapping result "
                    + "is not accessible. This is expected to be an error in the mapping implementation and should be fixed in there.")})
    @RequestMapping(value = {"/plugins/{pluginId}/execute"}, method = {RequestMethod.POST}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    void runPlugin(
            @Parameter(description = "The document to be mapped.", required = true) @RequestPart(name = "document") final MultipartFile document,
            @Parameter(description = "The mapping rules document.", required = true) @RequestPart(name = "mapping") final MultipartFile mapping,
            @Parameter(description = "The pluginId of the plugin to execute.", required = true) @PathVariable(value = "pluginId") String pluginId,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder) throws URISyntaxException;

    @RequestMapping(value = {"/schedule/"}, method = {RequestMethod.POST}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    ResponseEntity<JobStatus> scheduleMapDocument(
            @Parameter(description = "The mappingID of the already defined mapping.", required = true) @RequestParam(value = "mappingID") String mappingID,
            @Parameter(description = "The document to be mapped.", required = true) @RequestPart(name = "document") final MultipartFile document,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder) throws Throwable;

    @Operation(summary = "Map a document with an existing mapping.", description = "This endpoint allows the query a mapping job status for scheduled jobs.",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK is returned if the job status could be retrieved. The status document is sent in the body."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no mapping job for jobId could be found."),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if a parameter is missing or is not a valid UUID."),
                @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR if the mapping job has failed.")})

    @GetMapping(path = "/schedule/{job-id}/status", produces = "application/json")
    ResponseEntity<JobStatus> getJobStatus(
            @Parameter(description = "The jobId to query for.", required = true) @PathVariable(name = "job-id") String jobId) throws Throwable;

    @Operation(summary = "Get a mapping job's output file. The output file is available as soon as the job has finished. If this is the case, the job status "
            + "response contains the relative path pointing to the particular download for the job.",
            description = "This endpoint allows to download the result of an asynchronous job execution.",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK is returned if the job status could be retrieved. The status document is sent in the body."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no mapping job for jobId could be found."),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if a parameter is missing or is not a valid UUID."),
                @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR if the mapping job has failed.")})
    @GetMapping(path = "/schedule/{job-id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    ResponseEntity<Resource> getJobOutputFile(@PathVariable(name = "job-id") String jobId) throws Throwable;

    @Operation(summary = "Delete a mapping job's output file.",
            description = "This endpoint allows to remove the result of an asynchronous job execution from the server.",
            responses = {
                @ApiResponse(responseCode = "201", description = "NO_CONTENT is returned if the job removal succeeded."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no mapping job for jobId could be found."),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if a parameter is missing, is not a valid UUID, or if the job has not finished, yet."),
                @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR if the mapping job has failed.")})
    @DeleteMapping(path = "/schedule/{job-id}")
    ResponseEntity<Void> deleteJobAndAssociatedData(@PathVariable(name = "job-id") String jobId) throws Throwable;
}
