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
     * @param title The federal curriculum title
     * @param level The federal curriculum level
     * @param week The federal curriculum week
     * @param version The federal curriculum version
     * @param year The federal curriculum year
     * @param description The federal curriculum description
     * @return The created {@link Curriculum}
     * @throws Exception if creation fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    Curriculum create(
        String title,
        Curriculum.Level level,
        Curriculum.Week week,
        String version,
        String year,
        String description
    ) throws Exception;

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
     * @param title The federal curriculum title
     * @param level The federal curriculum level
     * @param week The federal curriculum week
     * @param version The federal curriculum version
     * @param year The federal curriculum year
     * @param description The federal curriculum description
     * @return Revised federal curriculum
     * @throws Exception if revision fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    default Curriculum revise(
        final long id,
        final String title,
        final Curriculum.Level level,
        final Curriculum.Week week,
        final String version,
        final String year,
        final String description
    ) throws Exception {
        return this.curriculum(id)
            .retitled(title)
            .releveled(level)
            .reweeked(week)
            .reversioned(version)
            .reyeared(year)
            .redescriptioned(description);
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
