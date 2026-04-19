/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

/**
 * Teacher capacity abstraction.
 *
 * <p>Represents how many hours per week a teacher can work.</p>
 *
 * @since 0.0.1
 */
public interface TeacherCapacity {

    /**
     * Capacity unique identifier.
     *
     * @return Capacity ID
     * @throws Exception if retrieval fails
     */
    long uid() throws Exception;

    /**
     * Schedule ID.
     *
     * @return Schedule ID
     * @throws Exception if retrieval fails
     */
    long scheduleId() throws Exception;

    /**
     * Teacher ID.
     *
     * @return Teacher ID
     * @throws Exception if retrieval fails
     */
    long teacherId() throws Exception;

    /**
     * Maximum hours per week.
     *
     * @return Maximum hours
     * @throws Exception if retrieval fails
     */
    int maxHoursPerWeek() throws Exception;

    /**
     * Minimum hours per week.
     *
     * @return Minimum hours or null
     * @throws Exception if retrieval fails
     */
    Integer minHoursPerWeek() throws Exception;
}
