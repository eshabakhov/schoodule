/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link Subject} entities.
 * <p>
 * Defines operations for creating, retrieving, updating, deleting,
 * listing, and counting subjects.
 *
 * @since 0.0.1
 */
public interface Subjects {

    /**
     * Creates a new subject with the given name.
     *
     * @param name The name of subject
     * @return The created {@link Subject}
     * @throws Exception if creation fails
     */
    Subject create(String name) throws Exception;

    /**
     * Finds a subject by its unique identifier.
     *
     * @param id The subject ID
     * @return The found {@link Subject}
     * @throws Exception if not found
     */
    Subject subject(long id) throws Exception;

    /**
     * Finds a subject by its name.
     *
     * @param name The subject name
     * @return The found {@link Subject}
     * @throws Exception if not found
     */
    Subject subject(String name) throws Exception;

    /**
     * Returns a paginated list of subjects optionally filtered by a search string.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link Subject} instances
     * @throws Exception if listing fails
     */
    PageableList<Subject> subjects(Condition condition, Page page) throws Exception;

    /**
     * Removes a subject by its ID.
     *
     * @param id The subject ID
     * @throws Exception if deletion fails
     */
    void remove(long id) throws Exception;
}
