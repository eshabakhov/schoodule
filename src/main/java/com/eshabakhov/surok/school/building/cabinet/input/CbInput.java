/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.cabinet.CabinetInput;

/**
 * Empty building cabinet input.
 *
 * @since 0.0.1
 */
public final class CbInput implements CabinetInput {

    /**
     * New empty input.
     */
    public CbInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
