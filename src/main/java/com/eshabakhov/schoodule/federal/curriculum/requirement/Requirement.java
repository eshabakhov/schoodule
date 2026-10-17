/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;

/**
 * Federal curriculum requirement domain entity.
 *
 * <p>Does not expose data via getters. Instead, prints itself
 * into a {@link Media} via {@link Printable#print(Media)}.</p>
 *
 * @since 0.0.1
 */
public interface Requirement extends Printable {

    /**
     * Returns the unique identifier of this requirement.
     *
     * @return Requirement ID
     */
    Long uid();

    /**
     * Revises this requirement.
     *
     * @param input Requirement input
     * @return Revised requirement
     * @throws Exception If revision fails
     */
    Requirement revise(RequirementInput input) throws Exception;

    /**
     * Parts of a federal curriculum requirement.
     *
     * @since 0.0.1
     */
    enum PartType {

        /**
         * Mandatory part of a curriculum plan.
         */
        MANDATORY,
        /**
         * Optional part of a curriculum plan.
         */
        OPTIONAL
    }
}
