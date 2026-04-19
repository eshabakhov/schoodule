/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link Cabinet} entities.
 * <p>
 * Provides methods for creating, retrieving, updating, deleting, listing,
 * and counting cabinets.
 *
 * @since 0.0.1
 */
public interface Cabinets {

    /**
     * Creates a new cabinet with the specified name.
     *
     * @param name The name of cabinet
     * @return The created {@link Cabinet}
     * @throws Exception if creation fails
     */
    Cabinet add(String name) throws Exception;

    /**
     * Finds a cabinet by its unique ID.
     *
     * @param id The cabinet ID
     * @return The found {@link Cabinet}
     * @throws Exception if not found
     */
    Cabinet find(long id) throws Exception;

    /**
     * Finds a cabinet by its name.
     *
     * @param name The cabinet name
     * @return The found {@link Cabinet}
     * @throws Exception if not found
     */
    Cabinet find(String name) throws Exception;

    /**
     * Lists cabinets optionally filtered by a search string.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link Cabinet} objects
     * @throws Exception if listing fails
     */
    PageableList<Cabinet> list(Condition condition, Page page) throws Exception;

    /**
     * Updates the name of a cabinet.
     *
     * @param id Cabinet ID
     * @param name The name of cabinet
     * @return The updated {@link Cabinet}
     * @throws Exception if update fails
     */
    Cabinet put(Long id, String name) throws Exception;

    /**
     * Removes a cabinet by its ID.
     *
     * @param id The cabinet ID
     * @throws Exception if deletion fails
     */
    void remove(long id) throws Exception;
}
