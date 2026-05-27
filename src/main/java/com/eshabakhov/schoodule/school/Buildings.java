/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link Building} entities.
 * <p>
 * Provides methods for creating, retrieving, deleting, listing buildings.
 *
 * @since 0.0.1
 */
public interface Buildings {

    /**
     * Creates a new building with the specified name.
     *
     * @param name The name of building
     * @return The created {@link Building}
     * @throws Exception if creation fails
     */
    Building create(String name) throws Exception;

    /**
     * Finds a building by its unique ID.
     *
     * @param id The building ID
     * @return The found {@link Building}
     * @throws Exception if not found
     */
    Building building(long id) throws Exception;

    /**
     * Finds a building by its name.
     *
     * @param name The building name
     * @return The found {@link Building}
     * @throws Exception if not found
     */
    Building building(String name) throws Exception;

    /**
     * Lists buildings optionally filtered by a search string.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link Building} objects
     * @throws Exception if listing fails
     */
    PageableList<Building> buildings(Condition condition, Page page) throws Exception;

    /**
     * Removes a building by its ID.
     *
     * @param id The building ID
     * @throws Exception if deletion fails
     */
    void remove(long id) throws Exception;
}
