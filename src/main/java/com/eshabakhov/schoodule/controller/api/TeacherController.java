/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.Teacher;
import com.eshabakhov.schoodule.school.teacher.ThBase;
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
 * Teacher's client controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/teachers")
@Tag(name = "Teachers")
public class TeacherController {

    /** JOOQ Table for Teacher. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** Simple type for version header. */
    private static final MediaType SIMPLE_TYPE = MediaType.valueOf(
        "application/com.eshabakhov.schoodule.school.teacher.simpleteacher+json"
    );

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    TeacherController(final DSLContext datasource) {
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
        summary = "Create teacher",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Teacher",
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
        description = "Teacher created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.teacher.simpleteacher+json",
            examples = {
                @ExampleObject(
                    name = "Simple teacher",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Petrov Petr Petrovich"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Teacher creation failed",
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
    public ResponseEntity<Teacher> create(
        @RequestHeader("version") final TeacherVersion version,
        @PathVariable final long school,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request teacher",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Petrov Petr Petrovich"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (TeacherVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new TeacherRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final Teacher teacher = new SlsPostgres(this.datasource)
                .find(school)
                .teachers()
                .create(new ThBase(name.asText()));
            return ResponseEntity
                .created(
                    URI.create(
                        String.format("/api/schools/%d/teachers/%d", school, teacher.uid())
                    )
                ).body(teacher);
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of teachers")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<Teacher>> list(
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
            name = "name_ct",
            required = false
        ) final String namect
    ) throws Exception {
        var condition = TeacherController.TEACHER.SCHOOL_ID.eq(school)
            .and(TeacherController.TEACHER.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                TeacherController.TEACHER.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity
            .ok()
            .body(
                new SlsPostgres(this.datasource)
                    .find(school)
                    .teachers()
                    .list(condition, new PageRequest(limit, offset))
            );
    }

    @GetMapping("/{teacher}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch teacher",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Teacher",
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
        description = "Schedule fetched",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.teacher.simpleteacher+json",
            examples = {
                @ExampleObject(
                    name = "Simple teacher",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Ivanov Ivan Ivanovich"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Teacher not found",
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
    public ResponseEntity<Teacher> get(
        @RequestHeader("version") final TeacherVersion version,
        @PathVariable final long school,
        @PathVariable final long teacher
    ) throws Exception {
        if (TeacherVersion.SIMPLE.equals(version)) {
            final var found = new SlsPostgres(this.datasource)
                .find(school)
                .teachers()
                .find(teacher);
            return ResponseEntity
                .ok()
                .contentType(TeacherController.SIMPLE_TYPE)
                .body(new ThBase(found.uid(), found.name()));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @PutMapping("/{teacher}")
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
        summary = "Update teacher",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Teacher",
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
        description = "Teacher updated",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.teacher.simpleteacher+json",
            examples = {
                @ExampleObject(
                    name = "Simple teacher",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Sidorov Sidr Sidorovich"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "Teacher created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.teacher.simpleteacher+json",
            examples = {
                @ExampleObject(
                    name = "Simple teacher",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Sidorov Sidr Sidorovich"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Teacher update failed",
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
    public ResponseEntity<Teacher> put(
        @RequestHeader("version") final TeacherVersion version,
        @PathVariable final long school,
        @PathVariable final long teacher,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request teacher",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "name": "Sidorov Sidr Sidorovich"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (TeacherVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new TeacherRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final var toupdate = new ThBase(teacher, name.asText());
            final var updated = new SlsPostgres(this.datasource)
                .find(school)
                .teachers()
                .put(new ThBase(teacher, name.asText()));
            final ResponseEntity<Teacher> response;
            if (toupdate.equals(updated)) {
                response = ResponseEntity.ok().body(updated);
            } else {
                response = ResponseEntity
                    .created(
                        URI.create(
                            String.format("/api/schools/%d/teachers/%d", school, updated.uid())
                        )
                    )
                    .body(updated);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @DeleteMapping("/{teacher}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove teacher")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long teacher
    ) throws Exception {
        new SlsPostgres(this.datasource)
            .find(school)
            .teachers()
            .remove(teacher);
        return ResponseEntity.noContent().build();
    }

    public static class TeacherRequiredFieldException extends Exception {
        public TeacherRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** Teacher accept version. */
    public enum TeacherVersion {

        /** Version of simple teacher. */
        SIMPLE
    }
}
