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
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface FederalCurriculums {

    /**
     * Creates a new federal curriculum.
     *
     * @param title The federal curriculum title
     * @param level The federal curriculum level
     * @param week The federal curriculum week
     * @param version The federal curriculum version
     * @param year The federal curriculum year
     * @param description The federal curriculum description
     * @return The created {@link FederalCurriculum}
     * @throws Exception if creation fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    FederalCurriculum create(
        String title,
        FederalCurriculum.Level level,
        FederalCurriculum.Week week,
        String version,
        String year,
        String description
    ) throws Exception;

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
     * Removes a federal curriculum by its ID.
     *
     * @param id The federal curriculum ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
