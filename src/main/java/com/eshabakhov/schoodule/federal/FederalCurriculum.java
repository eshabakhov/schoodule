/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirements;

/**
 * Federal curriculum plan domain entity.
 *
 * <p>Does not expose data via getters. Instead, prints itself
 * into a {@link Media} via {@link Printable#print(Media)}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface FederalCurriculum extends Printable {

    /**
     * Returns the unique identifier of this curriculum.
     *
     * @return Curriculum ID
     */
    Long uid();

    /**
     * Returns a new curriculum with an updated title.
     *
     * @param title New title
     * @return Updated curriculum
     */
    FederalCurriculum retitled(String title);

    /**
     * Returns a new curriculum with an updated education level.
     *
     * @param level New education level
     * @return Updated curriculum
     */
    FederalCurriculum releveled(Level level);

    /**
     * Returns a new curriculum with an updated study week type.
     *
     * @param week New study week type
     * @return Updated curriculum
     */
    FederalCurriculum reweeked(Week week);

    /**
     * Returns a new curriculum with an updated version.
     *
     * @param version New version
     * @return Updated curriculum
     */
    FederalCurriculum reversioned(String version);

    /**
     * Returns a new curriculum with an updated academic year.
     *
     * @param year New academic year
     * @return Updated curriculum
     */
    FederalCurriculum reyeared(String year);

    /**
     * Returns a new curriculum with an updated description.
     *
     * @param description New description
     * @return Updated curriculum
     */
    FederalCurriculum redescriptioned(String description);

    /**
     * Returns the requirements collection for this curriculum.
     *
     * @return Federal curriculum requirements
     */
    FederalCurriculumRequirements requirements();

    /**
     * Education levels supported by federal curriculum plans.
     *
     * @since 0.0.1
     */
    enum Level {
        /**
         * Primary general education, grades 1–4.
         */
        PRIMARY,
        /**
         * Basic general education, grades 5–9.
         */
        BASIC,
        /**
         * Secondary general education, grades 10–11.
         */
        SECONDARY
    }

    /**
     * Study week types supported by federal curriculum plans.
     *
     * @since 0.0.1
     */
    enum Week {
        /**
         * Five-day study week.
         */
        FIVE_DAYS,
        /**
         * Six-day study week.
         */
        SIX_DAYS
    }
}
