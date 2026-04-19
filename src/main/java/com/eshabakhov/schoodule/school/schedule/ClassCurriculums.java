/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import org.jooq.Condition;

/**
 * Interface for managing {@link ClassCurriculum} entities.
 *
 * <p>Provides methods for creating, retrieving, updating, deleting,
 * and listing class curriculums within a schedule.
 *
 * @since 0.0.1
 */
public interface ClassCurriculums {

    /**
     * Adds a new class curriculum.
     *
     * @param clazz The School class,
     * @param subject The subject,
     * @param hours The hours,
     * @return The created {@link ClassCurriculum}
     * @throws Exception if creation fails
     */
    ClassCurriculum add(SchoolClass clazz, Subject subject, Integer hours) throws Exception;

    /**
     * Finds a class curriculum by its unique ID.
     *
     * @param id The curriculum ID
     * @return The found {@link ClassCurriculum}
     * @throws Exception if not found
     */
    ClassCurriculum find(long id) throws Exception;

    /**
     * Lists class curriculums filtered by a condition.
     *
     * @param condition JOOQ condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link ClassCurriculum} objects
     * @throws Exception if listing fails
     */
    PageableList<ClassCurriculum> list(Condition condition, Page page) throws Exception;

    /**
     * Updates a class curriculum.
     *
     * @param id ID curriculum
     * @param clazz The School class,
     * @param subject The subject,
     * @param hours The hours,
     * @return The updated {@link ClassCurriculum}
     * @throws Exception if update fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    ClassCurriculum put(
        Long id,
        SchoolClass clazz,
        Subject subject,
        Integer hours
    ) throws Exception;

    /**
     * Removes a class curriculum by its ID.
     *
     * @param id The curriculum ID
     * @throws Exception if deletion fails
     */
    void remove(long id) throws Exception;
}
