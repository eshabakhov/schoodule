/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.Sorts;

/**
 * Interface for managing {@link Teacher} entities.
 *
 * @since 0.0.1
 */
public interface Teachers extends Printable {

    /**
     * Creates a new school teacher.
     *
     * @param input School teacher input
     * @return The created {@link Teacher}
     * @throws Exception if creation fails
     */
    Teacher create(TeacherInput input) throws Exception;

    /**
     * Finds a school teacher by its unique identifier.
     *
     * @param id The school teacher ID
     * @return The found {@link Teacher}
     * @throws Exception if not found
     */
    Teacher teacher(long id) throws Exception;

    /**
     * Revises a school teacher.
     *
     * @param id The school teacher ID
     * @param input School teacher input
     * @return Revised school teacher
     * @throws Exception if revision fails
     */
    default Teacher revise(
        final long id,
        final TeacherInput input
    ) throws Exception {
        return this.teacher(id).revise(input);
    }

    /**
     * Returns a paginated list of school teachers.
     *
     * @param filters Filters for selecting school teachers
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Teacher} instances
     * @throws Exception if listing fails
     */
    Teachers selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over school teachers represented by this collection.
     *
     * @return School teachers
     */
    Iterable<Teacher> iterate();

    /**
     * Removes a school teacher by its ID.
     *
     * @param id The school teacher ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
