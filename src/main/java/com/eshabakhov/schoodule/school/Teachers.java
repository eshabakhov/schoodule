/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link Teacher} entities.
 * <p>
 * Provides operations for creating, retrieving, updating, deleting,
 * listing, and counting teachers.
 *
 * @since 0.0.1
 */
public interface Teachers {

    /**
     * Creates a new teacher.
     *
     * @param name The name of teacher
     * @return The created {@link Teacher}
     */
    Teacher create(String name) throws Exception;

    /**
     * Finds a teacher by its unique identifier.
     *
     * @param id The teacher ID
     * @return The found {@link Teacher}
     */
    Teacher teacher(long id) throws Exception;

    /**
     * Finds a teacher by its name.
     *
     * @param name The teacher name
     * @return The found {@link Teacher}
     */
    Teacher teacher(String name) throws Exception;

    /**
     * Returns a paginated list of teachers optionally filtered by a search string.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link Teacher} instances
     */
    PageableList<Teacher> teachers(Condition condition, Page page) throws Exception;

    /**
     * Removes a teacher by its ID.
     *
     * @param id The teacher ID
     */
    void remove(long id) throws Exception;
}
