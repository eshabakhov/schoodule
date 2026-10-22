/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.BuildingInput;

/**
 * Empty school building input.
 *
 * @since 0.0.1
 */
public final class BdInput implements BuildingInput {

    /**
     * New empty input.
     */
    public BdInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
