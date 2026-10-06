/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.schedule.load.ClassLoad;
import com.eshabakhov.schoodule.school.schedule.load.ClassLoadSimple;
import com.eshabakhov.schoodule.school.schedule.load.LoadNotFoundException;
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
 * Class load REST API controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/schedules/{schedule}/loads")
@Tag(name = "Class Loads")
public class ClassLoadController {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    ClassLoadController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Create a class load entry.
     *
     * @param version Representation version
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param request Request body
     * @return Created load entry
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
        summary = "Create class load",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing a class load",
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
        description = "Class load created",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Simple load",
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
    @ApiResponse(responseCode = "400", description = "Class load creation failed")
    //@checkstyle ParameterNumberCheck (2 lines)
    public ResponseEntity<ClassLoad> create(
        @RequestHeader("version") final LoadVersion version,
        @PathVariable final long school,
        @PathVariable final long schedule,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request load",
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
        if (version == ClassLoadController.LoadVersion.SIMPLE) {
            final JsonNode classid = request.get("schoolClassId");
            final JsonNode subjectid = request.get("subjectId");
            final JsonNode hours = request.get("hoursPerWeek");
            if (classid == null || subjectid == null || hours == null) {
                throw new LoadRequiredFieldException(
                    "Fields 'schoolClassId', 'subjectId', 'hoursPerWeek' are required"
                );
            }
            final ClassLoad load = new SlsPostgres(this.ctx)
                .school(school)
                .schedules()
                .schedule(schedule)
                .loads().create(
                    new ScPostgres(this.ctx, classid.asLong()),
                    new SbPostgres(this.ctx, subjectid.asLong()),
                    hours.asInt()
                );
            return ResponseEntity.created(
                URI.create(
                    String.format(
                        "/api/schools/%d/schedules/%d/loads/%d",
                        school,
                        schedule,
                        load.uid()
                    )
                )
            )
            .body(load);
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    /**
     * Fetch class load entries.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param limit Page size
     * @param offset Page number
     * @return Class load page
     * @throws Exception When entries cannot be loaded
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of class loads")
    //@checkstyle ParameterNumberCheck (2 lines)
    public ResponseEntity<PageableList<ClassLoad>> list(
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
                .loads()
                .list(DSL.trueCondition(), new PageRequest(limit, offset))
        );
    }

    /**
     * Fetch a class load entry.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Load identifier
     * @return Class load
     * @throws Exception When the entry cannot be loaded
     */
    @GetMapping("/{load}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch class load")
    public ClassLoad get(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load
    ) throws Exception {
        return new SlsPostgres(this.ctx)
            .school(school)
            .schedules()
            .schedule(schedule)
            .loads()
            .load(load);
    }

    /**
     * Update a class load entry.
     *
     * @param version Representation version
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Load identifier
     * @param request Request body
     * @return Updated load entry
     * @throws Exception When the entry cannot be updated
     */
    @PutMapping("/{load}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Update class load")
    //@checkstyle ParameterNumberCheck (2 lines)
    public ResponseEntity<ClassLoad> put(
        @RequestHeader("version") final LoadVersion version,
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request load",
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
        if (version == ClassLoadController.LoadVersion.SIMPLE) {
            final JsonNode classid = request.get("schoolClassId");
            final JsonNode subjectid = request.get("subjectId");
            final JsonNode hours = request.get("hoursPerWeek");
            if (classid == null || subjectid == null || hours == null) {
                throw new LoadRequiredFieldException(
                    "Fields 'schoolClassId', 'subjectId', 'hoursPerWeek' are required"
                );
            }
            ResponseEntity<ClassLoad> response;
            try {
                final ClassLoad updated = new SlsPostgres(this.ctx)
                    .school(school)
                    .schedules()
                    .schedule(schedule)
                    .loads().load(load)
                    .allocate(hours.asInt())
                    .teach(new SbPostgres(this.ctx, subjectid.asLong()))
                    .target(new ScPostgres(this.ctx, classid.asLong()));
                response = ResponseEntity.ok().body(
                    new ClassLoadSimple(
                        updated.uid(),
                        updated.schoolClass(),
                        updated.subject(),
                        updated.hoursPerWeek()
                    )
                );
            } catch (final LoadNotFoundException ex) {
                final ClassLoad created = new SlsPostgres(this.ctx)
                    .school(school)
                    .schedules()
                    .schedule(schedule)
                    .loads().create(
                        new ScPostgres(this.ctx, classid.asLong()),
                        new SbPostgres(this.ctx, subjectid.asLong()),
                        hours.asInt()
                    );
                response = ResponseEntity.created(
                    URI.create(
                        String.format(
                            "/api/schools/%d/schedules/%d/loads/%d",
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
     * Delete a class load entry.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Load identifier
     * @return Empty response
     * @throws Exception When the entry cannot be deleted
     */
    @DeleteMapping("/{load}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove class load")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load
    ) throws Exception {
        new SlsPostgres(this.ctx)
            .school(school)
            .schedules()
            .schedule(schedule)
            .loads()
            .remove(load);
        return ResponseEntity.noContent().build();
    }

    /** Supported class load representation versions. */
    public enum LoadVersion {

        /** Version of simple load. */
        SIMPLE
    }
}
