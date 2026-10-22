/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.Sorts;

/**
 * Interface for managing {@link Cabinet} entities.
 *
 * @since 0.0.1
 */
public interface Cabinets extends Printable {

    /**
     * Creates a new building cabinet.
     *
     * @param input Building cabinet input
     * @return The created {@link Cabinet}
     * @throws Exception if creation fails
     */
    Cabinet create(CabinetInput input) throws Exception;

    /**
     * Finds a building cabinet by its unique identifier.
     *
     * @param id The building cabinet ID
     * @return The found {@link Cabinet}
     * @throws Exception if not found
     */
    Cabinet cabinet(long id) throws Exception;

    /**
     * Revises a building cabinet.
     *
     * @param id The building cabinet ID
     * @param input Building cabinet input
     * @return Revised building cabinet
     * @throws Exception if revision fails
     */
    default Cabinet revise(
        final long id,
        final CabinetInput input
    ) throws Exception {
        return this.cabinet(id).revise(input);
    }

    /**
     * Returns a paginated list of building cabinets.
     *
     * @param filters Filters for selecting building cabinets
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Cabinet} instances
     * @throws Exception if listing fails
     */
    Cabinets selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over building cabinets represented by this collection.
     *
     * @return Building cabinets
     */
    Iterable<Cabinet> iterate();

    /**
     * Removes a building cabinet by its ID.
     *
     * @param id The building cabinet ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
