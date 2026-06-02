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
@SuppressWarnings("PMD.TooManyMethods")
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
    Level level();

    /**
     * Returns a study week type of the federal curriculum.
     *
     * @return Study week type
     */
    Week week();

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
    String year();

    /**
     * Returns a description of the federal curriculum.
     *
     * @return Description
     */
    String description();

    /**
     * Returns a title of the federal curriculum.
     * @param title New title
     * @return Title
     */
    FederalCurriculum retitled(String title);

    /**
     * Returns an education level of the federal curriculum.
     * @param level New level
     * @return Education level
     */
    FederalCurriculum releveled(Level level);

    /**
     * Returns a study week type of the federal curriculum.
     * @param week New week
     * @return Study week type
     */
    FederalCurriculum reweeked(Week week);

    /**
     * Returns a version of the federal curriculum.
     * @param version New version
     * @return Version
     */
    FederalCurriculum reversioned(String version);

    /**
     * Returns an academic year of the federal curriculum.
     * @param year New year
     * @return Academic year
     */
    FederalCurriculum reyeared(String year);

    /**
     * Returns a description of the federal curriculum.
     * @param description New description
     * @return Description
     */
    FederalCurriculum redescriptioned(String description);

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
    enum Level {

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
    enum Week {

        /** Five-day study week. */
        FIVE_DAYS,

        /** Six-day study week. */
        SIX_DAYS
    }

}
