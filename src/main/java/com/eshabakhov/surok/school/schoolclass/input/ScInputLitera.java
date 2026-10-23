/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schoolclass.SchoolClassInput;

/**
 * School class input with a litera.
 *
 * @since 0.0.1
 */
public final class ScInputLitera implements SchoolClassInput {

    /** Origin input. */
    private final SchoolClassInput origin;

    /** SchoolClass litera. */
    private final String litera;

    /**
     * New litera input.
     *
     * @param origin Origin input
     * @param litera SchoolClass litera
     */
    public ScInputLitera(final SchoolClassInput origin, final String litera) {
        this.origin = origin;
        this.litera = litera;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.litera == null || this.litera.isBlank()) {
            throw new SchoolClassInputException(
                "SchoolClass litera is required"
            );
        }
        return this.origin.print(media).with("litera", this.litera.trim());
    }
}
