/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.federal.curriculum.CurriculumInput;

/**
 * Empty federal curriculum input.
 *
 * @since 0.0.1
 */
public final class CrInput implements CurriculumInput {

    /**
     * New empty input.
     */
    public CrInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
