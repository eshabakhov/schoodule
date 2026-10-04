/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.subject.SbBase;
import com.eshabakhov.schoodule.school.subject.SubjectNotFoundException;
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
 * Subject's client controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/subjects")
@Tag(name = "Subjects")
public class SubjectController {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** Simple type for version header. */
    private static final MediaType SIMPLE_TYPE = MediaType.valueOf(
        "application/com.eshabakhov.schoodule.school.subject.simplesubject+json"
    );

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    SubjectController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Create a subject.
     *
     * @param version Representation version
     * @param school School identifier
     * @param request Request body
     * @return Created subject
     * @throws Exception When the subject cannot be created
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
        summary = "Create subject",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Subject",
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
        description = "Subject created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.subject.simplesubject+json",
            examples = {
                @ExampleObject(
                    name = "Simple subject",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Literature"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Subject creation failed",
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
    public ResponseEntity<Subject> create(
        @RequestHeader("version") final SubjectVersion version,
        @PathVariable final long school,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request subject",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Math"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (version == SubjectController.SubjectVersion.SIMPLE) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new SubjectRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final Subject subject = new SlsPostgres(this.ctx)
                .school(school)
                .subjects()
                .create(name.asText());
            return ResponseEntity.created(
                URI.create(
                    String.format("/api/schools/%d/subjects/%d", school, subject.uid())
                )
            )
            .body(subject);
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Fetch subjects.
     *
     * @param school School identifier
     * @param limit Page size
     * @param offset Page number
     * @param namect Name filter
     * @return Subjects page
     * @throws Exception When subjects cannot be loaded
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of subjects")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<Subject>> list(
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
        Condition condition = SubjectController.SUBJECT.SCHOOL_ID.eq(school)
            .and(SubjectController.SUBJECT.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                SubjectController.SUBJECT.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity.ok().body(
            new SlsPostgres(this.ctx)
                .school(school)
                .subjects()
                .subjects(condition, new PageRequest(limit, offset))
        );
    }

    /**
     * Fetch a subject.
     *
     * @param version Representation version
     * @param school School identifier
     * @param subject Subject identifier
     * @return Subject
     * @throws Exception When the subject cannot be loaded
     */
    @GetMapping("/{subject}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch subject",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Subject",
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
        description = "Subject fetched",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.subject.simplesubject+json",
            examples = {
                @ExampleObject(
                    name = "Simple subject",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "PEE"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Subject not found",
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
    public ResponseEntity<Subject> get(
        @RequestHeader("version") final SubjectVersion version,
        @PathVariable final long school,
        @PathVariable final long subject
    ) throws Exception {
        if (version == SubjectController.SubjectVersion.SIMPLE) {
            final Subject found = new SlsPostgres(this.ctx)
                .school(school)
                .subjects()
                .subject(subject);
            return ResponseEntity
                .ok()
                .contentType(SubjectController.SIMPLE_TYPE)
                .body(new SbBase(found.uid(), found.name()));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Update a subject.
     *
     * @param version Representation version
     * @param school School identifier
     * @param subject Subject identifier
     * @param request Request body
     * @return Updated subject
     * @throws Exception When the subject cannot be updated
     */
    @PutMapping("/{subject}")
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
        summary = "Update subject",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Subject",
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
        description = "Subject updated",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.subject.simplesubject+json",
            examples = {
                @ExampleObject(
                    name = "Simple subject",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Algebra"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "Subject created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.subject.simplesubject+json",
            examples = {
                @ExampleObject(
                    name = "Simple subject",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Geometry"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Subject update failed",
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
    public ResponseEntity<Subject> put(
        @RequestHeader("version") final SubjectVersion version,
        @PathVariable final long school,
        @PathVariable final long subject,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request subject",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "name": "Biology"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (version == SubjectController.SubjectVersion.SIMPLE) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new SubjectRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            ResponseEntity<Subject> response;
            try {
                final Subject updated = new SlsPostgres(this.ctx)
                    .school(school)
                    .subjects()
                    .subject(subject)
                    .renamed(name.asText());
                response = ResponseEntity.ok().body(new SbBase(updated.uid(), updated.name()));
            } catch (final SubjectNotFoundException ex) {
                final Subject created = new SlsPostgres(this.ctx)
                    .school(school)
                    .subjects()
                    .create(name.asText());
                response = ResponseEntity.created(
                    URI.create(
                        String.format("/api/schools/%d/subjects/%d", school, created.uid())
                    )
                )
                .body(created);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Delete a subject.
     *
     * @param school School identifier
     * @param subject Subject identifier
     * @return Empty response
     * @throws Exception When the subject cannot be deleted
     */
    @DeleteMapping("/{subject}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove subject")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long subject
    ) throws Exception {
        new SlsPostgres(this.ctx)
            .school(school)
            .subjects()
            .remove(subject);
        return ResponseEntity.noContent().build();
    }

    /** Subject accept version. */
    public enum SubjectVersion {

        /** Version of simple subject. */
        SIMPLE
    }
}
