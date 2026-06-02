/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link FederalCurriculumRequirement} entities.
 *
 * @since 0.0.1
 */
public interface FederalCurriculumRequirements {

    /**
     * Creates a new federal curriculum requirement.
     *
     * @param grade The federal curriculum requirement grade
     * @param subject The federal curriculum requirement subject
     * @param hours The federal curriculum requirement hours
     * @param part The federal curriculum requirement part
     * @return The created {@link FederalCurriculumRequirement}
     * @throws Exception if creation fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    FederalCurriculumRequirement create(
        Integer grade,
        String subject,
        Integer hours,
        FederalCurriculumRequirement.PartType part
    ) throws Exception;

    /**
     * Finds a federal curriculum requirement by its unique identifier.
     *
     * @param id The federal curriculum requirement ID
     * @return The found {@link FederalCurriculumRequirement}
     * @throws Exception if not found
     */
    FederalCurriculumRequirement requirement(long id) throws Exception;

    /**
     * Returns a paginated list of federal curriculum requirements.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link FederalCurriculumRequirement} instances
     * @throws Exception if listing fails
     */
    PageableList<FederalCurriculumRequirement> requirements(Condition condition, Page page)
        throws Exception;

    /**
     * Removes a federal curriculum requirement by its ID.
     *
     * @param id The federal curriculum requirement ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
