/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.CurriculumInput;

/**
 * Empty federal curriculum input.
 *
 * @since 0.1.0
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
