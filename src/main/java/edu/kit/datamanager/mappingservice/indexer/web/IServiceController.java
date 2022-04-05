package edu.kit.datamanager.mappingservice.indexer.web;

import edu.kit.datamanager.mappingservice.indexer.domain.MappingRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;

@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "UNAUTHORIZED is returned if authorization in required but was not provided."),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN is returned if the caller has no sufficient privileges.")})
public interface IServiceController {

    @Operation(summary = "Map a document with an existing mapping.", description = "This endpoint allows the mapping of documents via a file upload. " +
            "The prerequisite for this is a mapping that has already been created in advance via the \"/api/v1/mapping\" endpoint. " +
            "The identifiers of this mapping must then be passed in this endpoint as parameters together with the document to be mapped as an upload. " +
            "The result is returned as a response and is not stored on the server." +
            "If no mappingType is specified, the mapping will be to elasticsearch. " +
            "The result of this mapping will be returned as response as well as transferred to the elasticsearch installation that was entered in the applications.properties of the server.", responses = {
            @ApiResponse(responseCode = "200", description = "OK is returned if the mapping was successful. " +
                    "The result will also be returned in the response.", content = @Content(schema = @Schema(implementation = MappingRecord.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST is returned if the mapping was not successful. " +
                    "The corresponding reason is returned in the response text. " +
                    "Possible reasons are a missing mapping or an unsuitable input.", content = @Content(mediaType = "String")),
            @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR is returned if errors occur that do not necessarily depend on user input, " +
                    "e.g. a faulty configuration. No more specific messages are returned for security reasons.")})

    @RequestMapping(value = {"/{mappingID}/{mappingType}"}, method = {RequestMethod.POST}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseBody
    ResponseEntity mapDocument(
            @Parameter(description = "The document to be mapped.", required = true) @RequestPart(name = "document") final MultipartFile document,
            @Parameter(description = "The mappingID of the already defined mapping schema.", required = true) @PathVariable(value = "mappingID") String mappingID,
            @Parameter(description = "The mappingType of the already defined mapping schema.", required = true) @PathVariable(name = "mappingType") String mappingType,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final UriComponentsBuilder uriBuilder) throws URISyntaxException;
}
