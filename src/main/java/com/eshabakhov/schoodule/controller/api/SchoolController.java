/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlBase;
import com.eshabakhov.schoodule.school.SlsPostgres;
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
import org.springframework.http.MediaType;
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
 * School's client controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools")
@Tag(name = "Schools")
public class SchoolController {

    /** JOOQ Table for School. */
    private static final com.eshabakhov.schoodule.tables.School SCHOOL =
        com.eshabakhov.schoodule.tables.School.SCHOOL;

    /** Simple type for version header. */
    private static final MediaType SIMPLE_TYPE =
        MediaType.valueOf("application/com.eshabakhov.schoodule.school.simpleschool+json");

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    SchoolController(final DSLContext datasource) {
        this.datasource = datasource;
    }

    /**
     * Create school.
     *
     * @param version Header version
     * @param request School payload
     * @return Response entity with created {@link School}
     * @throws Exception If creation fails
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create school",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing School",
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
        description = "School created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.simpleschool+json",
            examples = {
                @ExampleObject(
                    name = "Simple school",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool school"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "School creation failed",
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
    public ResponseEntity<School> create(
        @RequestHeader("version") final SchoolVersion version,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request school",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Cool school"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (SchoolVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new SchoolRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final School school = new SlsPostgres(this.datasource).create(name.asText());
            return ResponseEntity
                .created(URI.create(String.format("/api/schools/%d", school.uid())))
                .contentType(SchoolController.SIMPLE_TYPE)
                .body(new SlBase(school.uid(), school.name()));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Retrieve list of school.
     *
     * @param limit Limit schools
     * @param offset Offset for schools
     * @param namect Name of school which filtering on contains
     * @return Response entity with list of {@link School}
     * @throws Exception If retrieving fails
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetch list of schools")
    public ResponseEntity<PageableList<School>> list(
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
        var condition = SchoolController.SCHOOL.IS_DELETED.eq(false);
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                SchoolController.SCHOOL.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity
            .ok()
            .body(
                new SlsPostgres(this.datasource)
                    .list(condition, new PageRequest(limit, offset))
            );
    }

    /**
     * Retrieve school.
     *
     * @param version Header version of school
     * @param school School id
     * @return Response entity of {@link School}
     * @throws Exception If retrieving fails
     */
    @GetMapping("/{school}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch school",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing School",
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
        description = "School fetched",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.simpleschool+json",
            examples = {
                @ExampleObject(
                    name = "Simple school",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool school"
                        }"""
                )
            }
        )
    )
    public ResponseEntity<School> get(
        @RequestHeader("version") final SchoolVersion version,
        @PathVariable final long school
    ) throws Exception {
        if (SchoolVersion.SIMPLE.equals(version)) {
            final var found = new SlsPostgres(this.datasource).find(school);
            return ResponseEntity
                .ok()
                .contentType(SchoolController.SIMPLE_TYPE)
                .body(new SlBase(found.uid(), found.name()));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Update school.
     *
     * @param version Header version of school
     * @param school School id
     * @param request School payload
     * @return Response entity of {@link School}
     * @throws Exception If updating fails
     */
    @PutMapping("/{school}")
    @PreAuthorize(
        """
        (hasAnyRole('ADMIN', 'DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(
        summary = "Update school",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing School",
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
        description = "School updated",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.simpleschool+json",
            examples = {
                @ExampleObject(
                    name = "Simple school",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool school"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "School created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.simpleschool+json",
            examples = {
                @ExampleObject(
                    name = "Simple school",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool school"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "School update failed",
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
    public ResponseEntity<School> put(
        @RequestHeader("version") final SchoolVersion version,
        @PathVariable final long school,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request school",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Cool school"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (SchoolVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new SchoolRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final var toupdate = new SlBase(school, name.asText());
            final var updated = new SlsPostgres(this.datasource).put(toupdate);
            final ResponseEntity<School> response;
            if (toupdate.equals(updated)) {
                response = ResponseEntity
                    .ok()
                    .contentType(SchoolController.SIMPLE_TYPE)
                    .body(updated);
            } else {
                response = ResponseEntity
                    .created(URI.create(String.format("/api/schools/%d", updated.uid())))
                    .contentType(SchoolController.SIMPLE_TYPE)
                    .body(updated);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Remove school.
     *
     * @param school School id
     * @return Response 204
     * @throws Exception If removing fails
     * @since 0.0.1
     */
    @DeleteMapping("/{school}")
    @PreAuthorize(
        """
        (hasAnyRole('ADMIN', 'DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove school")
    public ResponseEntity<Void> delete(@PathVariable final long school) throws Exception {
        new SlsPostgres(this.datasource).remove(school);
        return ResponseEntity.noContent().build();
    }

    public static class SchoolRequiredFieldException extends Exception {
        public SchoolRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** School accept version. */
    public enum SchoolVersion {

        /** Version of simple school. */
        SIMPLE
    }
}
