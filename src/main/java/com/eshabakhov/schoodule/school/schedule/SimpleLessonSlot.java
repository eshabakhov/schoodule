/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple lesson slot implementation.
 *
 * <p>Immutable object representing lesson slot.</p>
 *
 * @since 0.0.1
 */
public final class SimpleLessonSlot implements LessonSlot {

    /**
     * Slot ID.
     */
    private final long identity;

    /**
     * Assignment ID.
     */
    private final long assignment;

    /**
     * Day of week.
     */
    private final String day;

    /**
     * Lesson number.
     */
    private final int lesson;

    /**
     * Cabinet ID.
     */
    private final Long cabinet;

    /**
     * Constructor for new slot.
     *
     * @param assignmentid Assignment ID
     * @param dayofweek Day of week
     * @param lessonnumber Lesson number
     * @param cabinetid Cabinet ID
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public SimpleLessonSlot(
        final long assignmentid,
        final String dayofweek,
        final int lessonnumber,
        final Long cabinetid
    ) {
        this(0L, assignmentid, dayofweek, lessonnumber, cabinetid);
    }

    /**
     * Full constructor.
     *
     * @param id Slot ID
     * @param assignmentid Assignment ID
     * @param dayofweek Day of week
     * @param lessonnumber Lesson number
     * @param cabinetid Cabinet ID
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    public SimpleLessonSlot(
        @JsonProperty("id") final long id,
        @JsonProperty("assignmentId") final long assignmentid,
        @JsonProperty("dayOfWeek") final String dayofweek,
        @JsonProperty("lessonNumber") final int lessonnumber,
        @JsonProperty("cabinetId") final Long cabinetid
    ) {
        this.identity = id;
        this.assignment = assignmentid;
        this.day = dayofweek;
        this.lesson = lessonnumber;
        this.cabinet = cabinetid;
    }

    @Override
    public long uid() {
        return this.identity;
    }

    @Override
    public long assignmentId() {
        return this.assignment;
    }

    @Override
    public String dayOfWeek() {
        return this.day;
    }

    @Override
    public int lessonNumber() {
        return this.lesson;
    }

    @Override
    public Long cabinetId() {
        return this.cabinet;
    }
}
