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

import edu.kit.datamanager.mappingservice.domain.MappingRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Interface and Documentation for mapping administration REST-API
 *
 * @author maximilianiKIT
 */
@ApiResponses(value = {
    @ApiResponse(responseCode = "401", description = "Unauthorized is returned if authorization in required but was not provided."),
    @ApiResponse(responseCode = "403", description = "Forbidden is returned if the caller has no sufficient privileges.")})
public interface IMappingAdministrationController {

    @Operation(summary = "Create a new mapping.", description = "This endpoint allows to create a new mapping and required two parameters: The record metadata, which contains "
            + "the mapping identifier and mapping type, and the mapping document, which defines the rules for the mapping applied by the given mapping type. ",
            responses = {
                @ApiResponse(responseCode = "201", description = "CREATED is returned only if the record has been validated, persisted and the mapping document was successfully validated and stored.", content = @Content(schema = @Schema(implementation = MappingRecord.class))),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if the provided mapping record or the mapping document is invalid."),
                @ApiResponse(responseCode = "409", description = "CONFLICT is returned, if there is already a mapping for the provided mapping id.")})

    @RequestMapping(path = "", method = RequestMethod.POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    ResponseEntity<MappingRecord> createMapping(
            @Parameter(description = "JSON representation of the mapping record.", required = true) @RequestPart(name = "record") final MultipartFile record,
            @Parameter(description = "The mapping document associated with the record. "
                    + "The format of the document is defined by the mapping type, which is given by the mappingType attribute of the mapping record.", required = true) @RequestPart(name = "document") final MultipartFile document,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder) throws URISyntaxException;

    @Operation(summary = "Get a mapping record by its identifier.", description = "Obtain is single mapping record by its identifier. "
            + "Depending on a user's role, accessing a specific record may be allowed or forbidden.",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK and the record are returned if the record exists and the user has sufficient permission.", content = @Content(schema = @Schema(implementation = MappingRecord.class))),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no record for the provided identifier was found.")})
    @RequestMapping(value = {"/{mappingId}"}, method = {RequestMethod.GET}, produces = {"application/vnd.datamanager.mapping-record+json"})
    @ResponseBody
    ResponseEntity<MappingRecord> getMappingById(
            @Parameter(description = "The mapping identifier.", required = true) @PathVariable(value = "mappingId") String mappingId,
            Pageable pgbl,
            WebRequest wr,
            HttpServletResponse hsr);

    @Operation(summary = "Get the mapping document associated with a given mapping identifier.", description = "Obtain a single mapping document identified by the mapping identifier. "
            + "Depending on a user's role, accessing a specific record may be allowed or forbidden. ",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK and the mapping document are returned if the mapping exists and the user has sufficient permission."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned, if no record for the provided identifier was found.")})

    @RequestMapping(value = {"/{mappingId}"}, method = {RequestMethod.GET})
    @ResponseBody
    ResponseEntity<MappingRecord> getMappingDocumentById(
            @Parameter(description = "The mapping identifier.", required = true) @PathVariable(value = "mappingId") String mappingId,
            WebRequest wr,
            HttpServletResponse hsr);

    @Operation(summary = "Get all mapping records.", description = "List all mapping records in a paginated and/or sorted form. The listing can be "
            + "refined by providing a typeId in order to return only mapping for a certain mapping type. If not typeId is provided, all mapping "
            + "records are returned.",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK and a list of records, which might be empty.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = MappingRecord.class))))})
    @RequestMapping(value = {""}, method = {RequestMethod.GET})
    @PageableAsQueryParam
    @ResponseBody
    ResponseEntity<List<MappingRecord>> getMappings(
            @Parameter(description = "The type identifier linked to a mapping type.") @RequestParam(value = "typeId") String typeId,
            Pageable pgbl,
            WebRequest wr,
            HttpServletResponse hsr,
            UriComponentsBuilder ucb);

    @Operation(summary = "Update a mapping record.", description = "Apply an update to the mapping record and/or the mapping document identified by provided identifier and/or its ",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK is returned in case of a successful update, e.g. the record (if provided) was in the correct format and the document (if provided) matches the provided schema id. The updated record is returned in the response.", content = @Content(schema = @Schema(implementation = MappingRecord.class))),
                @ApiResponse(responseCode = "400", description = "BAD_REQUEST is returned if the provided metadata record is invalid or if the validation using the provided schema failed."),
                @ApiResponse(responseCode = "404", description = "NOT_FOUND is returned if no record for the provided identifier was found.")})
    @RequestMapping(value = "/{mappingId}", method = RequestMethod.PUT, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {"application/json"})
    @Parameters({
        @Parameter(name = "If-Match", description = "ETag of the current mapping record. Please use quotation marks!", required = true, in = ParameterIn.HEADER)})
    @ResponseBody
    ResponseEntity<MappingRecord> updateMapping(
            @Parameter(description = "The mapping identifier.", required = true) @PathVariable(value = "mappingId") String mappingId,
            @Parameter(description = "JSON representation of the metadata record.") @RequestPart(name = "record") final MultipartFile record,
            @Parameter(description = "The mapping document associated with the record. "
                    + "The format of the document is defined by the mapping type, which is given by the mappingType attribute of the mapping record.", required = false) @RequestPart(name = "document") final MultipartFile document,
            final WebRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder
    );

    @Operation(summary = "Delete a mapping.", description = "Delete a single mapping record and the associated mapping document. "
            + "Deleting a record typically requires the caller to have special permissions. "
            + "In some cases, deleting a record can also be available for the owner or other privileged users or can be forbidden at all.",
            responses = {
                @ApiResponse(responseCode = "204", description = "NO_CONTENT is returned as long as no error occurs while deleting a record.")})
    @RequestMapping(value = {"/{mappingId}"}, method = {RequestMethod.DELETE})
    @Parameters({
        @Parameter(name = "If-Match", description = "ETag of the current mapping record. Please use quotation marks!", required = true, in = ParameterIn.HEADER)})
    @ResponseBody
    ResponseEntity<String> deleteMapping(
            @Parameter(description = "The mapping identifier.", required = true) @PathVariable(value = "mappingId") String mappingId,
            WebRequest wr,
            HttpServletResponse hsr);

    @Operation(summary = "Get all available mapping types.",
            responses = {
                @ApiResponse(responseCode = "200", description = "OK and a list of all mapping types will be returned, which might be empty.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = PluginInformation.class))))})
    @RequestMapping(value = {"/types"}, method = {RequestMethod.GET})
    @ResponseBody
    ResponseEntity<List<PluginInformation>> getAllAvailableMappingTypes(
            WebRequest wr,
            HttpServletResponse hsr);

    @Operation(summary = "Reload all mapping types.", description = "Reloads all mapping types from the plugin directory and updates their dependencies if necessary.",
            responses = {
                @ApiResponse(responseCode = "204", description = "NO_CONTENT is returned on a successful refresh.")})
    @RequestMapping(value = {"/reloadTypes"}, method = {RequestMethod.GET})
    ResponseEntity<String> reloadAllAvailableMappingTypes(
            WebRequest wr,
            HttpServletResponse hsr);
}
