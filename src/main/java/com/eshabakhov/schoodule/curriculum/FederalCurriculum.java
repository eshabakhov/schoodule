/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Jsonable;

/**
 * Federal curriculum plan domain entity interface.
 *
 * @since 0.0.1
 */
public interface FederalCurriculum extends Jsonable {

    /**
     * Returns a FederalCurriculum unique identifier.
     *
     * @return FederalCurriculum's ID
     */
    Long uid();

    /**
     * Returns a title of the federal curriculum.
     *
     * @return Title
     */
    String title();

    /**
     * Returns an education level of the federal curriculum.
     *
     * @return Education level
     */
    EducationLevel educationLevel();

    /**
     * Returns a study week type of the federal curriculum.
     *
     * @return Study week type
     */
    StudyWeek studyWeekType();

    /**
     * Returns a version of the federal curriculum.
     *
     * @return Version
     */
    String version();

    /**
     * Returns an academic year of the federal curriculum.
     *
     * @return Academic year
     */
    String academicYear();

    /**
     * Returns a description of the federal curriculum.
     *
     * @return Description
     */
    String description();

    /**
     * Returns collection of federal curriculum requirements.
     *
     * @return Federal curriculum requirements collection
     */
    FederalCurriculumRequirements requirements();

    /**
     * Education levels supported by federal curriculum plans.
     *
     * @since 0.0.1
     */
    enum EducationLevel {

        /** Primary general education, grades 1-4. */
        PRIMARY,

        /** Basic general education, grades 5-9. */
        BASIC,

        /** Secondary general education, grades 10-11. */
        SECONDARY
    }

    /**
     * Study week types supported by federal curriculum plans.
     *
     * @since 0.0.1
     */
    enum StudyWeek {

        /** Five-day study week. */
        FIVE_DAYS,

        /** Six-day study week. */
        SIX_DAYS
    }

}
