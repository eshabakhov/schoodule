/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.Sorts;

/**
 * Interface for managing {@link Curriculum} entities.
 *
 * @since 0.0.1
 */
public interface Curriculums extends Printable {

    /**
     * Creates a new federal curriculum.
     *
     * @param input Federal curriculum input
     * @return The created {@link Curriculum}
     * @throws Exception if creation fails
     */
    Curriculum create(CurriculumInput input) throws Exception;

    /**
     * Finds a federal curriculum by its unique identifier.
     *
     * @param id The federal curriculum ID
     * @return The found {@link Curriculum}
     * @throws Exception if not found
     */
    Curriculum curriculum(long id) throws Exception;

    /**
     * Revises a federal curriculum.
     *
     * @param id The federal curriculum ID
     * @param input Federal curriculum input
     * @return Revised federal curriculum
     * @throws Exception if revision fails
     */
    default Curriculum revise(
        final long id,
        final CurriculumInput input
    ) throws Exception {
        return this.curriculum(id).revise(input);
    }

    /**
     * Returns a paginated list of federal curriculums.
     *
     * @param filters Filters for selecting federal curriculums
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Curriculum} instances
     * @throws Exception if listing fails
     */
    Curriculums selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over federal curriculums represented by this collection.
     *
     * @return Federal curriculums
     */
    Iterable<Curriculum> iterate();

    /**
     * Removes a federal curriculum by its ID.
     *
     * @param id The federal curriculum ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
