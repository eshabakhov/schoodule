/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.Sorts;

/**
 * Interface for managing {@link Building} entities.
 *
 * @since 0.0.1
 */
public interface Buildings extends Printable {

    /**
     * Creates a new school building.
     *
     * @param input School building input
     * @return The created {@link Building}
     * @throws Exception if creation fails
     */
    Building create(BuildingInput input) throws Exception;

    /**
     * Finds a school building by its unique identifier.
     *
     * @param id The school building ID
     * @return The found {@link Building}
     * @throws Exception if not found
     */
    Building building(long id) throws Exception;

    /**
     * Revises a school building.
     *
     * @param id The school building ID
     * @param input School building input
     * @return Revised school building
     * @throws Exception if revision fails
     */
    default Building revise(
        final long id,
        final BuildingInput input
    ) throws Exception {
        return this.building(id).revise(input);
    }

    /**
     * Returns a paginated list of school buildings.
     *
     * @param filters Filters for selecting school buildings
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Building} instances
     * @throws Exception if listing fails
     */
    Buildings selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over school buildings represented by this collection.
     *
     * @return School buildings
     */
    Iterable<Building> iterate();

    /**
     * Removes a school building by its ID.
     *
     * @param id The school building ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
