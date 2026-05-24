/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Jsonable;

/**
 * Federal curriculum requirement domain entity interface.
 *
 * @since 0.0.1
 */
public interface FederalCurriculumRequirement extends Jsonable {

    /**
     * Returns a FederalCurriculumRequirement unique identifier.
     *
     * @return FederalCurriculumRequirement's ID
     */
    Long uid();

    /**
     * Returns the parent federal curriculum.
     *
     * @return Federal curriculum
     */
    FederalCurriculum curriculum();

    /**
     * Returns a school grade.
     *
     * @return Grade
     */
    Integer grade();

    /**
     * Returns a subject name.
     *
     * @return Subject name
     */
    String subjectName();

    /**
     * Returns the number of hours per week.
     *
     * @return Weekly hours
     */
    Integer weeklyHours();

    /**
     * Returns a curriculum part type.
     *
     * @return Curriculum part type
     */
    PartType partType();

    /**
     * Parts of a federal curriculum requirement.
     *
     * @since 0.0.1
     */
    enum PartType {

        /** Mandatory part of a curriculum plan. */
        MANDATORY,

        /** Optional part of a curriculum plan. */
        OPTIONAL
    }

}
