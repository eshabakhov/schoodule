/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.federal.curriculum.requirement.RequirementInput;

/**
 * Empty federal curriculum requirement input.
 *
 * @since 0.0.1
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
