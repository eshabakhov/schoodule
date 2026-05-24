/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link FederalCurriculum} entities.
 *
 * @since 0.0.1
 */
public interface FederalCurriculums {

    /**
     * Creates a new federal curriculum.
     *
     * @param curriculum The federal curriculum
     * @return The created {@link FederalCurriculum}
     * @throws Exception if creation fails
     */
    FederalCurriculum create(FederalCurriculum curriculum) throws Exception;

    /**
     * Finds a federal curriculum by its unique identifier.
     *
     * @param id The federal curriculum ID
     * @return The found {@link FederalCurriculum}
     * @throws Exception if not found
     */
    FederalCurriculum curriculum(long id) throws Exception;

    /**
     * Returns a paginated list of federal curriculums.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link FederalCurriculum} instances
     * @throws Exception if listing fails
     */
    PageableList<FederalCurriculum> curriculums(Condition condition, Page page) throws Exception;

    /**
     * Updates an existing federal curriculum.
     *
     * @param curriculum The federal curriculum
     * @return The updated {@link FederalCurriculum}
     * @throws Exception if update fails
     */
    FederalCurriculum put(FederalCurriculum curriculum) throws Exception;

    /**
     * Removes a federal curriculum by its ID.
     *
     * @param id The federal curriculum ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
