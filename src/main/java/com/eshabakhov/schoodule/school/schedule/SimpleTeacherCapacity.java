/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple teacher capacity implementation.
 *
 * <p>Immutable object representing teacher capacity.</p>
 *
 * @since 0.0.1
 */
public final class SimpleTeacherCapacity implements TeacherCapacity {

    /**
     * Capacity ID.
     */
    private final long identity;

    /**
     * Schedule ID.
     */
    private final long schedule;

    /**
     * Teacher ID.
     */
    private final long teacher;

    /**
     * Maximum hours per week.
     */
    private final int maximum;

    /**
     * Minimum hours per week.
     */
    private final Integer minimum;

    /**
     * Constructor for new capacity.
     *
     * @param scheduleid Schedule ID
     * @param teacherid Teacher ID
     * @param max Maximum hours
     */
    public SimpleTeacherCapacity(
        final long scheduleid,
        final long teacherid,
        final int max
    ) {
        this(0L, scheduleid, teacherid, max, null);
    }

    /**
     * Full constructor.
     *
     * @param id Capacity ID
     * @param scheduleid Schedule ID
     * @param teacherid Teacher ID
     * @param max Maximum hours
     * @param min Minimum hours
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public SimpleTeacherCapacity(
        @JsonProperty("id") final long id,
        @JsonProperty("scheduleId") final long scheduleid,
        @JsonProperty("teacherId") final long teacherid,
        @JsonProperty("maxHoursPerWeek") final int max,
        @JsonProperty("minHoursPerWeek") final Integer min
    ) {
        this.identity = id;
        this.schedule = scheduleid;
        this.teacher = teacherid;
        this.maximum = max;
        this.minimum = min;
    }

    @Override
    public long uid() {
        return this.identity;
    }

    @Override
    public long scheduleId() {
        return this.schedule;
    }

    @Override
    public long teacherId() {
        return this.teacher;
    }

    @Override
    public int maxHoursPerWeek() {
        return this.maximum;
    }

    @Override
    public Integer minHoursPerWeek() {
        return this.minimum;
    }
}
