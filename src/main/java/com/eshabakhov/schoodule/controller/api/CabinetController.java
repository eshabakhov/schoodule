/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Cabinet;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.cabinet.CbBase;
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
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/cabinets")
@Tag(name = "Cabinets")
public class CabinetController {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    CabinetController(final DSLContext datasource) {
        this.datasource = datasource;
    }

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
        if (CabinetVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new CabinetRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final Cabinet cabinet = new SlsPostgres(this.datasource)
                .find(school)
                .cabinets()
                .add(name.asText());
            return ResponseEntity
                .created(
                    URI.create(
                        String.format("/api/schools/%d/cabinets/%d", school, cabinet.uid())
                    )
                )
                .body(new CbBase(cabinet));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of cabinets")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<Cabinet>> list(
        @PathVariable final long school,
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
        var condition = CabinetController.CABINET.SCHOOL_ID.eq(school)
            .and(CabinetController.CABINET.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                CabinetController.CABINET.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity
            .ok()
            .body(
                new SlsPostgres(this.datasource)
                    .find(school)
                    .cabinets()
                    .list(condition, new PageRequest(limit, offset))
            );
    }

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
        @PathVariable final long cabinet
    ) throws Exception {
        return new SlsPostgres(this.datasource).find(school)
            .cabinets().find(cabinet);
    }

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
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<Cabinet> put(
        @RequestHeader("version") final CabinetVersion version,
        @PathVariable final long school,
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
        if (CabinetVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new CabinetRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final var updated = new SlsPostgres(this.datasource)
                .find(school)
                .cabinets()
                .put(cabinet, name.asText());
            final ResponseEntity<Cabinet> response;
            if (cabinet == updated.uid()) {
                response = ResponseEntity.ok().body(updated);
            } else {
                response = ResponseEntity
                    .created(
                        URI.create(
                            String.format("/api/schools/%d/cabinets/%d", school, updated.uid())
                        )
                    )
                    .body(new CbBase(updated));
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

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
        @PathVariable final long cabinet
    ) throws Exception {
        new SlsPostgres(this.datasource)
            .find(school)
            .cabinets()
            .remove(cabinet);
        return ResponseEntity.noContent().build();
    }

    public static class CabinetRequiredFieldException extends Exception {
        public CabinetRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** Cabinet accept version. */
    public enum CabinetVersion {

        /** Version of simple cabinet. */
        SIMPLE
    }
}
