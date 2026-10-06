/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.Sorts;

/**
 * Interface for managing {@link Requirement} entities.
 *
 * @since 0.0.1
 */
public interface Requirements extends Printable {

    /**
     * Creates a new federal curriculum requirement.
     *
     * @param grade The federal curriculum requirement grade
     * @param subject The federal curriculum requirement subject
     * @param hours The federal curriculum requirement hours
     * @param part The federal curriculum requirement part
     * @return The created {@link Requirement}
     * @throws Exception if creation fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    Requirement create(
        Integer grade,
        String subject,
        Integer hours,
        Requirement.PartType part
    ) throws Exception;

    /**
     * Finds a federal curriculum requirement by its unique identifier.
     *
     * @param id The federal curriculum requirement ID
     * @return The found {@link Requirement}
     * @throws Exception if not found
     */
    Requirement requirement(long id) throws Exception;

    /**
     * Returns a paginated list of federal curriculum requirements.
     *
     * @param filters Filters for selecting federal curriculum requirements
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Requirement} instances
     * @throws Exception if listing fails
     */
    Requirements selection(
        Filters filters,
        Page page,
        Sorts sorts
    ) throws Exception;

    /**
     * Iterates over requirements represented by this collection.
     *
     * @return Federal curriculum requirements
     */
    Iterable<Requirement> iterate();

    /**
     * Removes a federal curriculum requirement by its ID.
     *
     * @param id The federal curriculum requirement ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
