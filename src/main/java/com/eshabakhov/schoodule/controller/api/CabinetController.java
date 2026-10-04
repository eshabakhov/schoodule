/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.building.Cabinet;
import com.eshabakhov.schoodule.school.building.cabinet.CbBase;
import com.eshabakhov.schoodule.school.building.cabinet.CbsPostgres;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cabinet's client controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/buildings/{building}/cabinets/")
@Tag(name = "Cabinets")
public class CabinetController {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    CabinetController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Create a cabinet.
     *
     * @param version Representation version
     * @param school School identifier
     * @param building Building identifier
     * @param request Request body
     * @return Created cabinet
     * @throws Exception When the cabinet cannot be created
     */
    @PostMapping
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(
        summary = "Create cabinet",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Cabinet",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = "SIMPLE"
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "201",
        description = "Cabinet created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.cabinet.simplecabinet+json",
            examples = {
                @ExampleObject(
                    name = "Simple cabinet",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool cabinet"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Cabinet creation failed",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Field is required and cannot be empty",
                    summary = "Required field",
                    value = """
                        {
                            "message": "Field 'name' is required and cannot be empty",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "406",
        description = "Not acceptable header",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Version header is incorrect",
                    summary = "Incorrect header",
                    value = """
                        {
                            "message": "Method parameter 'version' is incorrect",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    public ResponseEntity<Cabinet> create(
        @RequestHeader("version") final CabinetVersion version,
        @PathVariable final long school,
        @PathVariable final long building,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request cabinet",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Cool cabinet"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (version == CabinetController.CabinetVersion.SIMPLE) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new CabinetRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final Cabinet cabinet = new SlsPostgres(this.ctx)
                .school(school)
                .buildings()
                .building(building)
                .cabinets()
                .create(name.asText());
            return ResponseEntity.created(
                URI.create(
                    String.format(
                        "/api/schools/%d/buildings/%d/cabinets/%d",
                        school, building, cabinet.uid()
                    )
                )
            )
            .body(new CbBase(cabinet.uid(), cabinet.name()));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Fetch cabinets.
     *
     * @param school School identifier
     * @param building Building identifier
     * @param limit Page size
     * @param offset Page number
     * @param namect Name filter
     * @return Cabinets page
     * @throws Exception When cabinets cannot be loaded
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of cabinets")
    public ResponseEntity<PageableList<Cabinet>> list(
        @PathVariable final long school,
        @PathVariable final long building,
        @RequestParam(
            name = "limit",
            required = false,
            defaultValue = "10"
        ) final int limit,
        @RequestParam(
            name = "offset",
            required = false,
            defaultValue = "1"
        ) final int offset,
        @RequestParam(
            value = "name_ct",
            required = false
        ) final String namect
    ) throws Exception {
        Condition condition = CabinetController.CABINET.BUILDING_ID.eq(building)
            .and(CabinetController.CABINET.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                CabinetController.CABINET.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity.ok().body(
            new SlsPostgres(this.ctx)
                .school(school)
                .buildings()
                .building(building)
                .cabinets()
                .cabinets(condition, new PageRequest(limit, offset))
        );
    }

    /**
     * Fetch a cabinet.
     *
     * @param school School identifier
     * @param building Building identifier
     * @param cabinet Cabinet identifier
     * @return Cabinet
     * @throws Exception When the cabinet cannot be loaded
     */
    @GetMapping("/{cabinet}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch cabinet",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Cabinet",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = "SIMPLE"
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cabinet fetched",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.cabinet.simplecabinet+json",
            examples = {
                @ExampleObject(
                    name = "Simple cabinet",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool cabinet"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Cabinet not found",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(
        responseCode = "406",
        description = "Not acceptable header",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Version header is incorrect",
                    summary = "Incorrect header",
                    value = """
                        {
                            "message": "Method parameter 'version' is incorrect",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    public Cabinet get(
        @PathVariable final long school,
        @PathVariable final long building,
        @PathVariable final long cabinet
    ) throws Exception {
        return new SlsPostgres(this.ctx)
            .school(school)
            .buildings()
            .building(building)
            .cabinets()
            .cabinet(cabinet);
    }

    /**
     * Update a cabinet.
     *
     * @param version Representation version
     * @param school School identifier
     * @param building Building identifier
     * @param cabinet Cabinet identifier
     * @param request Request body
     * @return Updated cabinet
     * @throws Exception When the cabinet cannot be updated
     */
    @PutMapping("/{cabinet}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(
        summary = "Update cabinet",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Cabinet",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = "SIMPLE"
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Cabinet updated",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.cabinet.simplecabinet+json",
            examples = {
                @ExampleObject(
                    name = "Simple cabinet",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool cabinet"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "Cabinet created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.cabinet.simplecabinet+json",
            examples = {
                @ExampleObject(
                    name = "Simple cabinet",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool cabinet"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Cabinet update failed",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Field is required and cannot be empty",
                    summary = "Required field",
                    value = """
                        {
                            "message": "Field 'name' is required and cannot be empty",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "406",
        description = "Not acceptable header",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Version header is incorrect",
                    summary = "Incorrect header",
                    value = """
                        {
                            "message": "Method parameter 'version' is incorrect",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    public ResponseEntity<Cabinet> put(
        @RequestHeader("version") final CabinetVersion version,
        @PathVariable final long school,
        @PathVariable final long building,
        @PathVariable final long cabinet,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request cabinet",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "name": "Cool cabinet"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (version == CabinetController.CabinetVersion.SIMPLE) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new CabinetRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            ResponseEntity<Cabinet> response;
            try {
                response = ResponseEntity.ok().body(
                    new SlsPostgres(this.ctx)
                        .school(school)
                        .buildings()
                        .building(building)
                        .cabinets()
                        .cabinet(cabinet)
                        .renamed(name.asText())
                );
            } catch (final CbsPostgres.CabinetNotFoundException ex) {
                final Cabinet newcabinet = new SlsPostgres(this.ctx)
                    .school(school)
                    .buildings()
                    .building(building)
                    .cabinets()
                    .create(name.asText());
                response = ResponseEntity.created(
                    URI.create(
                        String.format(
                            "/api/schools/%d/buildings/%d/cabinets/%d",
                            school, building, newcabinet.uid()
                        )
                    )
                )
                .body(newcabinet);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Delete a cabinet.
     *
     * @param school School identifier
     * @param building Building identifier
     * @param cabinet Cabinet identifier
     * @return Empty response
     * @throws Exception When the cabinet cannot be deleted
     */
    @DeleteMapping("/{cabinet}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove cabinet")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long building,
        @PathVariable final long cabinet
    ) throws Exception {
        new SlsPostgres(this.ctx)
            .school(school)
            .buildings()
            .building(building)
            .cabinets()
            .remove(cabinet);
        return ResponseEntity.noContent().build();
    }

    /** Cabinet accept version. */
    public enum CabinetVersion {

        /** Version of simple cabinet. */
        SIMPLE
    }
}
