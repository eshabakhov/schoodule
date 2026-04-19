/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.schedule.SimpleSchedule;
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
 * Schedule's client controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/schedules")
@Tag(name = "Schedule")
public class ScheduleController {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** Simple type for version header. */
    private static final MediaType SIMPLE_TYPE = MediaType.valueOf(
        "application/com.eshabakhov.schoodule.school.schedule.simpleschedule+json"
    );

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    ScheduleController(final DSLContext datasource) {
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
        summary = "Create schedule",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Schedule",
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
        description = "Schedule created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.schedule.simpleschedule+json",
            examples = {
                @ExampleObject(
                    name = "Simple schedule",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Best schedule"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Schedule creation failed",
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
    public ResponseEntity<Schedule> create(
        @RequestHeader("version") final ScheduleVersion version,
        @PathVariable final long school,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request schedule",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Best schedule"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (ScheduleVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new ScheduleRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final Schedule schedule = new SlsPostgres(this.datasource)
                .find(school)
                .schedules()
                .add(name.asText());
            return ResponseEntity
                .created(
                    URI.create(
                        String.format("/api/schools/%d/schedules/%d", school, schedule.uid())
                    )
                )
                .body(new SimpleSchedule(schedule));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of schedules")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<Schedule>> list(
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
        var condition = ScheduleController.SCHEDULE.SCHOOL_ID.eq(school)
            .and(ScheduleController.SCHEDULE.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                ScheduleController.SCHEDULE.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity
            .ok()
            .body(
                new SlsPostgres(this.datasource)
                    .find(school)
                    .schedules()
                    .list(condition, new PageRequest(limit, offset))
            );
    }

    @GetMapping("/{schedule}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch schedule",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Schedule",
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
            mediaType = "application/com.eshabakhov.schoodule.school.schedule.simpleschedule+json",
            examples = {
                @ExampleObject(
                    name = "Simple schedule",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Best schedule"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Schedule not found",
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
    public ResponseEntity<Schedule> get(
        @RequestHeader("version") final ScheduleVersion version,
        @PathVariable final long school,
        @PathVariable final long schedule
    ) throws Exception {
        if (ScheduleVersion.SIMPLE.equals(version)) {
            return ResponseEntity
                .ok()
                .contentType(ScheduleController.SIMPLE_TYPE)
                .body(
                    new SimpleSchedule(
                        new SlsPostgres(this.datasource)
                            .find(school)
                            .schedules()
                            .find(schedule)
                    )
                );
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @PutMapping("/{schedule}")
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
        summary = "Update schedule",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Schedule",
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
        description = "Schedule updated",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.schedule.simpleschedule+json",
            examples = {
                @ExampleObject(
                    name = "Simple schedule",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Best schedule"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "Schedule created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.schedule.simpleschedule+json",
            examples = {
                @ExampleObject(
                    name = "Simple schedule",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Best schedule"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Schedule update failed",
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
    public ResponseEntity<Schedule> put(
        @RequestHeader("version") final ScheduleVersion version,
        @PathVariable final long school,
        @PathVariable final long schedule,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request schedule",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "name": "Best schedule"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (ScheduleVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new ScheduleRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final var updated = new SlsPostgres(this.datasource)
                .find(school)
                .schedules()
                .put(schedule, name.asText());
            final ResponseEntity<Schedule> response;
            if (schedule == updated.uid()) {
                response = ResponseEntity.ok().body(new SimpleSchedule(updated));
            } else {
                response = ResponseEntity
                    .created(
                        URI.create(
                            String.format("/api/schools/%d/schedules/%d", school, updated.uid())
                        )
                    )
                    .body(new SimpleSchedule(updated));
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @DeleteMapping("/{schedule}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove schedule")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long schedule
    ) throws Exception {
        new SlsPostgres(this.datasource)
            .find(school)
            .schedules()
            .remove(schedule);
        return ResponseEntity.noContent().build();
    }

    public static class ScheduleRequiredFieldException extends Exception {
        public ScheduleRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** Schedule accept version. */
    public enum ScheduleVersion {

        /** Version of simple schedule. */
        SIMPLE
    }
}
