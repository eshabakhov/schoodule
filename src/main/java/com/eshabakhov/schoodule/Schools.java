/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import org.jooq.Condition;

/**
 * Interface representing a collection of schools.
 *
 * @since 0.0.1
 */
public interface Schools {

    /**
     * Creates a new school in the collection.
     *
     * @param name Name of school
     * @return The newly created school
     */
    School create(String name) throws Exception;

    /**
     * Retrieves a school from the collection by its ID.
     *
     * @param id Unique identifier of the school
     * @return School with the specified ID
     */
    School find(long id) throws Exception;

    /**
     * Retrieves a school from the collection by its exact name.
     *
     * @param name Exact name of the school
     * @return School with the specified name
     */
    School find(String name) throws Exception;

    /**
     * Retrieves schools from the collection.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return Collection of schools
     */
    PageableList<School> list(Condition condition, Page page)  throws Exception;

    /**
     * Updates an existing school or creates a new one with specified ID.
     *
     * @param school Updated school with unchanged ID
     * @return Updated or newly created school
     */
    School put(School school) throws Exception;

    /**
     * Removes a school from the collection.
     *
     * @param school ID of the school to remove
     */
    void remove(long school) throws Exception;
}
