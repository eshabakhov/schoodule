/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link Schedule} entities.
 * <p>
 * Provides operations for creating, retrieving, updating, deleting,
 * listing, checking existence, and counting schedules.
 *
 * @since 0.0.1
 */
public interface Schedules {

    /**
     * Creates a new schedule with the given name.
     *
     * @param name The schedule name
     * @return The created {@link Schedule}
     * @throws Exception if creation fails
     */
    Schedule create(String name) throws Exception;

    /**
     * Finds a schedule by its unique identifier.
     *
     * @param id The schedule ID
     * @return The found {@link Schedule}
     * @throws Exception if not found
     */
    Schedule schedule(long id) throws Exception;

    /**
     * Finds a schedule by its name.
     *
     * @param name The schedule name
     * @return The found {@link Schedule}
     * @throws Exception if not found
     */
    Schedule schedule(String name) throws Exception;

    /**
     * Returns a paginated list of schedules optionally filtered by a search string.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link Schedule} instances
     * @throws Exception if listing fails
     */
    PageableList<Schedule> schedules(Condition condition, Page page) throws Exception;

    /**
     * Removes a schedule by its ID.
     *
     * @param id The schedule ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
