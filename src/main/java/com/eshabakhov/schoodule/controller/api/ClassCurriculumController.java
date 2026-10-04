/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;
import com.eshabakhov.schoodule.school.schedule.curriculum.CsCrSimple;
import com.eshabakhov.schoodule.school.schedule.curriculum.CurriculumNotFoundException;
import com.eshabakhov.schoodule.school.schoolclass.ScPostgres;
import com.eshabakhov.schoodule.school.subject.SbPostgres;
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
import org.jooq.impl.DSL;
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
 * ClassCurriculum REST API controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/schedules/{schedule}/curriculum")
@Tag(name = "Class Curriculums")
public class ClassCurriculumController {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    ClassCurriculumController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Create a class curriculum entry.
     *
     * @param version Representation version
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param request Request body
     * @return Created curriculum entry
     * @throws Exception When the entry cannot be created
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
        summary = "Create class curriculum",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing ClassCurriculum",
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
        description = "ClassCurriculum created",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Simple curriculum",
                    value = """
                        {
                            "id": 1,
                            "scheduleId": 1,
                            "schoolClassId": 5,
                            "subjectId": 3,
                            "hoursPerWeek": 5
                        }"""
                )
            }
        )
    )
    @ApiResponse(responseCode = "400", description = "ClassCurriculum creation failed")
    //@checkstyle ParameterNumberCheck (2 lines)
    public ResponseEntity<ClassCurriculum> create(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long school,
        @PathVariable final long schedule,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request curriculum",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "schoolClassId": 5,
                                "subjectId": 3,
                                "hoursPerWeek": 5
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (version == ClassCurriculumController.CurriculumVersion.SIMPLE) {
            final JsonNode classid = request.get("schoolClassId");
            final JsonNode subjectid = request.get("subjectId");
            final JsonNode hours = request.get("hoursPerWeek");
            if (classid == null || subjectid == null || hours == null) {
                throw new CurriculumRequiredFieldException(
                    "Fields 'schoolClassId', 'subjectId', 'hoursPerWeek' are required"
                );
            }
            final ClassCurriculum curriculum = new SlsPostgres(this.ctx)
                .school(school)
                .schedules()
                .schedule(schedule)
                .curriculums().create(
                    new ScPostgres(this.ctx, classid.asLong()),
                    new SbPostgres(this.ctx, subjectid.asLong()),
                    hours.asInt()
                );
            return ResponseEntity.created(
                URI.create(
                    String.format(
                        "/api/schools/%d/schedules/%d/curriculum/%d",
                        school,
                        schedule,
                        curriculum.uid()
                    )
                )
            )
            .body(curriculum);
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Fetch class curriculum entries.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param limit Page size
     * @param offset Page number
     * @return Curriculum page
     * @throws Exception When entries cannot be loaded
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of class curriculums")
    //@checkstyle ParameterNumberCheck (2 lines)
    public ResponseEntity<PageableList<ClassCurriculum>> list(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam(name = "limit", required = false, defaultValue = "10") final int limit,
        @RequestParam(name = "offset", required = false, defaultValue = "1") final int offset
    ) throws Exception {
        return ResponseEntity.ok().body(
            new SlsPostgres(this.ctx)
                .school(school)
                .schedules()
                .schedule(schedule)
                .curriculums()
                .list(DSL.trueCondition(), new PageRequest(limit, offset))
        );
    }

    /**
     * Fetch a class curriculum entry.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param curriculum Curriculum identifier
     * @return Curriculum entry
     * @throws Exception When the entry cannot be loaded
     */
    @GetMapping("/{curriculum}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch class curriculum")
    public ClassCurriculum get(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long curriculum
    ) throws Exception {
        return new SlsPostgres(this.ctx)
            .school(school)
            .schedules()
            .schedule(schedule)
            .curriculums()
            .curriculum(curriculum);
    }

    /**
     * Update a class curriculum entry.
     *
     * @param version Representation version
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param curriculum Curriculum identifier
     * @param request Request body
     * @return Updated curriculum entry
     * @throws Exception When the entry cannot be updated
     */
    @PutMapping("/{curriculum}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Update class curriculum")
    //@checkstyle ParameterNumberCheck (2 lines)
    public ResponseEntity<ClassCurriculum> put(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long curriculum,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request curriculum",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "schoolClassId": 5,
                                "subjectId": 3,
                                "hoursPerWeek": 6
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (version == ClassCurriculumController.CurriculumVersion.SIMPLE) {
            final JsonNode classid = request.get("schoolClassId");
            final JsonNode subjectid = request.get("subjectId");
            final JsonNode hours = request.get("hoursPerWeek");
            if (classid == null || subjectid == null || hours == null) {
                throw new CurriculumRequiredFieldException(
                    "Fields 'schoolClassId', 'subjectId', 'hoursPerWeek' are required"
                );
            }
            ResponseEntity<ClassCurriculum> response;
            try {
                final ClassCurriculum updated = new SlsPostgres(this.ctx)
                    .school(school)
                    .schedules()
                    .schedule(schedule)
                    .curriculums().curriculum(curriculum)
                    .allocate(hours.asInt())
                    .teach(new SbPostgres(this.ctx, subjectid.asLong()))
                    .target(new ScPostgres(this.ctx, classid.asLong()));
                response = ResponseEntity.ok().body(
                    new CsCrSimple(
                        updated.uid(),
                        updated.schoolClass(),
                        updated.subject(),
                        updated.hoursPerWeek()
                    )
                );
            } catch (final CurriculumNotFoundException ex) {
                final ClassCurriculum created = new SlsPostgres(this.ctx)
                    .school(school)
                    .schedules()
                    .schedule(schedule)
                    .curriculums().create(
                        new ScPostgres(this.ctx, classid.asLong()),
                        new SbPostgres(this.ctx, subjectid.asLong()),
                        hours.asInt()
                    );
                response = ResponseEntity.created(
                    URI.create(
                        String.format(
                            "/api/schools/%d/schedules/%d/curriculum/%d",
                            school,
                            schedule,
                            created.uid()
                        )
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
     * Delete a class curriculum entry.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param curriculum Curriculum identifier
     * @return Empty response
     * @throws Exception When the entry cannot be deleted
     */
    @DeleteMapping("/{curriculum}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove class curriculum")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long curriculum
    ) throws Exception {
        new SlsPostgres(this.ctx)
            .school(school)
            .schedules()
            .schedule(schedule)
            .curriculums()
            .remove(curriculum);
        return ResponseEntity.noContent().build();
    }

    /** ClassCurriculum accept version. */
    public enum CurriculumVersion {

        /** Version of simple curriculum. */
        SIMPLE
    }
}
