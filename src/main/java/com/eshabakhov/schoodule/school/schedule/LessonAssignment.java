/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

/**
 * Lesson assignment abstraction.
 *
 * <p>Represents assignment of teacher to teach specific
 * curriculum hours.</p>
 *
 * @since 0.0.1
 */
public interface LessonAssignment {

    /**
     * Assignment unique identifier.
     *
     * @return Assignment ID
     * @throws Exception if retrieval fails
     */
    long uid() throws Exception;

    /**
     * Class curriculum ID.
     *
     * @return Curriculum ID
     * @throws Exception if retrieval fails
     */
    long curriculumId() throws Exception;

    /**
     * Teacher ID.
     *
     * @return Teacher ID
     * @throws Exception if retrieval fails
     */
    long teacherId() throws Exception;

    /**
     * Hours per week assigned to this teacher.
     *
     * @return Hours per week
     * @throws Exception if retrieval fails
     */
    int hoursPerWeek() throws Exception;
}
