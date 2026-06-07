/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;

/**
 * Federal curriculum requirement domain entity.
 *
 * <p>Does not expose data via getters. Instead, prints itself
 * into a {@link Media} via {@link Printable#print(Media)}.
 *
 * @since 0.0.1
 */
public interface FederalCurriculumRequirement extends Printable {

    /**
     * Returns the unique identifier of this requirement.
     *
     * @return Requirement ID
     */
    Long uid();

    /**
     * Returns a new requirement with an updated grade.
     *
     * @param grade New grade
     * @return Updated requirement
     */
    FederalCurriculumRequirement regraded(Integer grade);

    /**
     * Returns a new requirement with an updated subject name.
     *
     * @param subject New subject name
     * @return Updated requirement
     */
    FederalCurriculumRequirement resubjected(String subject);

    /**
     * Returns a new requirement with an updated weekly hours count.
     *
     * @param hours New weekly hours
     * @return Updated requirement
     */
    FederalCurriculumRequirement reweekled(Integer hours);

    /**
     * Returns a new requirement with an updated curriculum part type.
     *
     * @param part New part type
     * @return Updated requirement
     */
    FederalCurriculumRequirement reparted(PartType part);

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
