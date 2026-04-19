/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.schoolclass.ScBase;
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
 * SchoolClass's client controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/classes")
@Tag(name = "School classes")
public class SchoolClassController {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass SCHOOL_CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** Simple type for version header. */
    private static final MediaType SIMPLE_TYPE = MediaType.valueOf(
        "application/com.eshabakhov.schoodule.school.schoolclass.simpleschoolclass+json"
    );

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    SchoolClassController(final DSLContext datasource) {
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
        summary = "Create school class",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing School class",
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
        description = "School class created",
        content = @Content(
            mediaType =
                "application/com.eshabakhov.schoodule.school.schoolclass.simpleschoolclass+json",
            examples = {
                @ExampleObject(
                    name = "Simple school class",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "5F"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "School class creation failed",
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
    public ResponseEntity<SchoolClass> create(
        @RequestHeader("version") final SchoolClassVersion version,
        @PathVariable final long school,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request school class",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "5F"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (SchoolClassVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new SchoolClassRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final SchoolClass clazz = new SlsPostgres(this.datasource)
                .find(school)
                .schoolClasses()
                .add(name.asText());
            return ResponseEntity
                .created(
                    URI.create(
                        String.format("/api/schools/%d/classes/%d", school, clazz.uid())
                    )
                )
                .body(new ScBase(clazz));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of school classes")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<SchoolClass>> list(
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
        var condition = SchoolClassController.SCHOOL_CLASS.SCHOOL_ID.eq(school)
            .and(SchoolClassController.SCHOOL_CLASS.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                SchoolClassController.SCHOOL_CLASS.NAME.likeIgnoreCase(
                    String.format("%%%s%%", namect)
                )
            );
        }
        return ResponseEntity
            .ok()
            .body(
                new SlsPostgres(this.datasource)
                    .find(school)
                    .schoolClasses()
                    .list(condition, new PageRequest(limit, offset))
            );
    }

    @GetMapping("/{clazz}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch school class",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing School class",
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
        description = "School class fetched",
        content = @Content(
            mediaType =
                "application/com.eshabakhov.schoodule.school.schoolclass.simpleschoolclass+json",
            examples = {
                @ExampleObject(
                    name = "Simple school class",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "5F"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "School class not found",
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
    public ResponseEntity<SchoolClass> get(
        @RequestHeader("version") final SchoolClassVersion version,
        @PathVariable final long school,
        @PathVariable final long clazz
    ) throws Exception {
        if (SchoolClassVersion.SIMPLE.equals(version)) {
            return ResponseEntity
                .ok()
                .contentType(SchoolClassController.SIMPLE_TYPE)
                .body(
                    new ScBase(
                        new SlsPostgres(this.datasource)
                            .find(school)
                            .schoolClasses()
                            .find(clazz)
                    )
                );
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @PutMapping("/{clazz}")
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
        summary = "Update school class",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing School class",
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
        description = "School class updated",
        content = @Content(
            mediaType =
                "application/com.eshabakhov.schoodule.school.schoolclass.simpleschoolclass+json",
            examples = {
                @ExampleObject(
                    name = "Simple school class",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "5F"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "School class created",
        content = @Content(
            mediaType =
                "application/com.eshabakhov.schoodule.school.schoolclass.simpleschoolclass+json",
            examples = {
                @ExampleObject(
                    name = "Simple school class",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "5F"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "School class update failed",
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
    public ResponseEntity<SchoolClass> put(
        @RequestHeader("version") final SchoolClassVersion version,
        @PathVariable final long school,
        @PathVariable final long clazz,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request school class",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "name": "5F"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (SchoolClassVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new SchoolClassRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final var updated = new SlsPostgres(this.datasource)
                .find(school)
                .schoolClasses()
                .put(clazz, name.asText());
            final ResponseEntity<SchoolClass> response;
            if (clazz == updated.uid()) {
                response = ResponseEntity.ok().body(new ScBase(updated));
            } else {
                response = ResponseEntity
                    .created(
                        URI.create(
                            String.format("/api/schools/%d/classes/%d", school, updated.uid())
                        )
                    )
                    .body(new ScBase(updated));
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @DeleteMapping("/{clazz}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove school class")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long clazz
    ) throws Exception {
        new SlsPostgres(this.datasource)
            .find(school)
            .schoolClasses()
            .remove(clazz);
        return ResponseEntity.noContent().build();
    }

    public static class SchoolClassRequiredFieldException extends Exception {
        public SchoolClassRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** School class accept version. */
    public enum SchoolClassVersion {

        /** Version of simple school class. */
        SIMPLE
    }
}
