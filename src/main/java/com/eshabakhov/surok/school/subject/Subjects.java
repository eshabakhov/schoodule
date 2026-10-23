/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.subject;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.Sorts;

/**
 * Interface for managing {@link Subject} entities.
 *
 * @since 0.0.1
 */
public interface Subjects extends Printable {

    /**
     * Creates a new school subject.
     *
     * @param input School subject input
     * @return The created {@link Subject}
     * @throws Exception if creation fails
     */
    Subject create(SubjectInput input) throws Exception;

    /**
     * Finds a school subject by its unique identifier.
     *
     * @param id The school subject ID
     * @return The found {@link Subject}
     * @throws Exception if not found
     */
    Subject subject(long id) throws Exception;

    /**
     * Revises a school subject.
     *
     * @param id The school subject ID
     * @param input School subject input
     * @return Revised school subject
     * @throws Exception if revision fails
     */
    default Subject revise(
        final long id,
        final SubjectInput input
    ) throws Exception {
        return this.subject(id).revise(input);
    }

    /**
     * Returns a paginated list of school subjects.
     *
     * @param filters Filters for selecting school subjects
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Subject} instances
     * @throws Exception if listing fails
     */
    Subjects selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over school subjects represented by this collection.
     *
     * @return School subjects
     */
    Iterable<Subject> iterate();

    /**
     * Removes a school subject by its ID.
     *
     * @param id The school subject ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
