/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RequirementInput;

/**
 * Empty federal curriculum requirement input.
 *
 * @since 0.1.0
 */
public final class RqInput implements RequirementInput {

    /**
     * New empty input.
     */
    public RqInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
