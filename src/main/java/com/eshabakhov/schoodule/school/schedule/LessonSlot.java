/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

/**
 * Lesson slot abstraction.
 *
 * <p>Represents specific lesson placement in schedule grid.</p>
 *
 * @since 0.0.1
 */
public interface LessonSlot {

    /**
     * Slot unique identifier.
     *
     * @return Slot ID
     * @throws Exception if retrieval fails
     */
    long uid() throws Exception;

    /**
     * Lesson assignment ID.
     *
     * @return Assignment ID
     * @throws Exception if retrieval fails
     */
    long assignmentId() throws Exception;

    /**
     * Day of week.
     *
     * @return Day
     * @throws Exception if retrieval fails
     */
    String dayOfWeek() throws Exception;

    /**
     * Lesson number.
     *
     * @return Lesson number
     * @throws Exception if retrieval fails
     */
    int lessonNumber() throws Exception;

    /**
     * Cabinet ID.
     *
     * @return Cabinet ID or null
     * @throws Exception if retrieval fails
     */
    Long cabinetId() throws Exception;
}
