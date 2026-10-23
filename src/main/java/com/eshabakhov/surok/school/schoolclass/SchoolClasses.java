/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.Sorts;

/**
 * Interface for managing {@link SchoolClass} entities.
 *
 * @since 0.0.1
 */
public interface SchoolClasses extends Printable {

    /**
     * Creates a new school class.
     *
     * @param input School class input
     * @return The created {@link SchoolClass}
     * @throws Exception if creation fails
     */
    SchoolClass create(SchoolClassInput input) throws Exception;

    /**
     * Finds a school class by its unique identifier.
     *
     * @param id The school class ID
     * @return The found {@link SchoolClass}
     * @throws Exception if not found
     */
    SchoolClass clazz(long id) throws Exception;

    /**
     * Revises a school class.
     *
     * @param id The school class ID
     * @param input School class input
     * @return Revised school class
     * @throws Exception if revision fails
     */
    default SchoolClass revise(
        final long id,
        final SchoolClassInput input
    ) throws Exception {
        return this.clazz(id).revise(input);
    }

    /**
     * Returns a paginated list of school classes.
     *
     * @param filters Filters for selecting school classes
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link SchoolClass} instances
     * @throws Exception if listing fails
     */
    SchoolClasses selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over school classes represented by this collection.
     *
     * @return School classes
     */
    Iterable<SchoolClass> iterate();

    /**
     * Removes a school class by its ID.
     *
     * @param id The school class ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
