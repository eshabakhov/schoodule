/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.load.ClassLoadInput;

/**
 * Empty teaching load input.
 *
 * @since 0.0.1
 */
public final class ClInput implements ClassLoadInput {

    /**
     * New empty input.
     */
    public ClInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
