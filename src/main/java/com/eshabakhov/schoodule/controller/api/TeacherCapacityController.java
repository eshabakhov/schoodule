/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.school.schedule.SimpleTeacherCapacity;
import com.eshabakhov.schoodule.school.schedule.TeacherCapacity;
import com.eshabakhov.schoodule.user.AuthUser;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Teacher capacity API controller.
 *
 * <p>Manages teacher workload limits.</p>
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schedules/{schedule}/capacity")
@Tag(name = "Teacher Capacity")
public class TeacherCapacityController {

    /**
     * Database context.
     */
    private final DSLContext datasource;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public TeacherCapacityController(final DSLContext dsl) {
        this.datasource = dsl;
    }

    /**
     * Creates a workload capacity constraint for a teacher within a schedule.
     *
     * <p>Defines the maximum (and optionally minimum) number of teaching hours
     * per week allowed for the specified teacher.</p>
     *
     * <p>The {@code teacherId} and {@code maxHoursPerWeek} fields are mandatory.
     * The {@code minHoursPerWeek} field is optional.</p>
     *
     * @param schedule Schedule identifier
     * @param user Authenticated user
     * @param request Request body containing teacher capacity parameters
     * @return Response entity with created capacity and {@code 201 Created} status
     * @throws CapacityException If required fields are missing
     * @throws IllegalStateException If capacity creation fails
     * @throws Exception If database interaction fails
     */
    @PostMapping
    @PreAuthorize(
        "hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')"
    )
    @Operation(summary = "Set teacher capacity")
    @ApiResponse(
        responseCode = "201",
        description = "Capacity set",
        content = @Content(
            examples = @ExampleObject(
                """
                {
                    "id": 1,
                    "scheduleId": 5,
                    "teacherId": 8,
                    "maxHoursPerWeek": 20,
                    "minHoursPerWeek": 10
                }"""
            )
        )
    )
    public ResponseEntity<TeacherCapacity> create(
        @PathVariable final long schedule,
        @AuthenticationPrincipal final AuthUser user,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                examples = @ExampleObject(
                    """
                    {
                        "teacherId": 8,
                        "maxHoursPerWeek": 20,
                        "minHoursPerWeek": 10
                    }"""
                )
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        this.ensureScheduleAccess(user, schedule);
        final JsonNode teachernode = request.get("teacherId");
        final JsonNode maxnode = request.get("maxHoursPerWeek");
        if (teachernode == null || maxnode == null) {
            throw new CapacityException("Teacher and max hours are required");
        }
        final JsonNode minnode = request.get("minHoursPerWeek");
        final Integer min;
        if (minnode != null && !minnode.isNull()) {
            min = minnode.asInt();
        } else {
            min = null;
        }
        final var record = this.datasource
            .resultQuery(
                """
                insert into teacher_capacity(
                    schedule_id,
                    teacher_id,
                    max_hours_per_week,
                    min_hours_per_week
                )
                values (?, ?, ?, ?)
                returning
                    id,
                    schedule_id,
                    teacher_id,
                    max_hours_per_week,
                    min_hours_per_week
                """,
                schedule,
                teachernode.asLong(),
                maxnode.asInt(),
                min
            )
            .fetchOne();
        if (record == null) {
            throw new IllegalStateException("Failed to create capacity");
        }
        final var created = TeacherCapacityController.capacity(record);
        return ResponseEntity
            .created(
                URI.create(
                    String.format(
                        "/api/schedules/%d/capacity/%d",
                        schedule,
                        created.uid()
                    )
                )
            )
            .body(created);
    }

    /**
     * Returns all teacher capacity constraints for the given schedule.
     *
     * <p>The result contains workload limits for all teachers
     * associated with the specified schedule.</p>
     *
     * @param schedule Schedule identifier
     * @param user Authenticated user
     * @return Response entity with list of teacher capacities
     */
    @GetMapping
    @Operation(summary = "Get all teacher capacities for schedule")
    public ResponseEntity<List<TeacherCapacity>> list(
        @PathVariable final long schedule,
        @AuthenticationPrincipal final AuthUser user
    ) {
        this.ensureScheduleAccess(user, schedule);
        final var capacities = this.datasource
            .resultQuery(
                """
                select
                    id,
                    schedule_id,
                    teacher_id,
                    max_hours_per_week,
                    min_hours_per_week
                from teacher_capacity
                where schedule_id = ?
                """,
                schedule
            )
            .fetch()
            .<TeacherCapacity>map(TeacherCapacityController::capacity);
        return ResponseEntity.ok(capacities);
    }

    /**
     * Updates workload limits for an existing teacher capacity entry.
     *
     * <p>The {@code maxHoursPerWeek} field is mandatory.
     * The {@code minHoursPerWeek} field is optional and may be cleared.</p>
     *
     * @param schedule Schedule identifier
     * @param capacity Teacher capacity identifier
     * @param user Authenticated user
     * @param request Request body containing updated capacity values
     * @return Response entity with updated teacher capacity
     * @throws CapacityException If max hours is missing or capacity not found
     * @throws Exception If database interaction fails
     */
    //@checkstyle ParameterNumberCheck (7 lines)
    @PutMapping("/{capacity}")
    @PreAuthorize(
        "hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')"
    )
    @Operation(summary = "Update teacher capacity")
    public ResponseEntity<TeacherCapacity> update(
        @PathVariable final long schedule,
        @PathVariable final long capacity,
        @AuthenticationPrincipal final AuthUser user,
        @RequestBody final JsonNode request
    ) throws Exception {
        this.ensureScheduleAccess(user, schedule);
        final JsonNode maxnode = request.get("maxHoursPerWeek");
        if (maxnode == null) {
            throw new CapacityException("Max hours is required");
        }
        final JsonNode minnode = request.get("minHoursPerWeek");
        final Integer min;
        if (minnode != null && !minnode.isNull()) {
            min = minnode.asInt();
        } else {
            min = null;
        }
        this.datasource
            .query(
                """
                update teacher_capacity
                set
                    max_hours_per_week = ?,
                    min_hours_per_week = ?
                where id = ?
                """,
                maxnode.asInt(),
                min,
                capacity
            )
            .execute();
        final var record = this.datasource
            .resultQuery(
                """
                select
                    id,
                    schedule_id,
                    teacher_id,
                    max_hours_per_week,
                    min_hours_per_week
                from teacher_capacity
                where id = ?
                """,
                capacity
            )
            .fetchOne();
        if (record == null) {
            throw new CapacityException("Capacity not found");
        }
        return ResponseEntity.ok(TeacherCapacityController.capacity(record));
    }

    /**
     * Deletes a teacher capacity constraint.
     *
     * <p>Removes workload limits associated with the specified teacher
     * from the schedule.</p>
     *
     * @param schedule Schedule identifier
     * @param capacity Teacher capacity identifier
     * @param user Authenticated user
     * @return Empty response with {@code 204 No Content} status
     */
    @DeleteMapping("/{capacity}")
    @PreAuthorize(
        "hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')"
    )
    @Operation(summary = "Delete teacher capacity")
    public ResponseEntity<Void> delete(
        @PathVariable final long schedule,
        @PathVariable final long capacity,
        @AuthenticationPrincipal final AuthUser user
    ) {
        this.ensureScheduleAccess(user, schedule);
        this.datasource
            .query(
                """
                delete from teacher_capacity
                where id = ?
                """,
                capacity
            )
            .execute();
        return ResponseEntity.noContent().build();
    }

    private void ensureScheduleAccess(final AuthUser user, final long schedule) {
        if (user.isAdmin()) {
            return;
        }
        final var school = this.datasource
            .resultQuery(
                """
                select school_id
                from schedule
                where id = ?
                """,
                schedule
            )
            .fetchOne("school_id", Long.class);
        if (school == null || school.longValue() != user.info().school()) {
            throw new AccessDeniedException("You do not have permission to access this schedule");
        }
    }

    private static TeacherCapacity capacity(final Record record) {
        return new SimpleTeacherCapacity(
            record.get("id", Long.class),
            record.get("schedule_id", Long.class),
            record.get("teacher_id", Long.class),
            record.get("max_hours_per_week", Integer.class),
            record.get("min_hours_per_week", Integer.class)
        );
    }

    /**
     * Capacity exception.
     *
     * @since 0.0.1
     */
    public static class CapacityException extends Exception {

        /**
         * Constructor.
         *
         * @param message Error message
         */
        public CapacityException(final String message) {
            super(message);
        }
    }
}
