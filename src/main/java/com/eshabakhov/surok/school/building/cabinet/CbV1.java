/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.Media;

/**
 * Version 1 implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
public final class CbV1 implements Cabinet {

    /**
     * Original cabinet.
     */
    private final Cabinet origin;

    /**
     * Creates a version 1 cabinet.
     *
     * @param origin Original cabinet
     */
    public CbV1(final Cabinet origin) {
        this.origin = origin;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).include(
            "id", "name"
        );
    }

    @Override
    public Cabinet revise(final CabinetInput input) throws Exception {
        return this.origin.revise(input);
    }
}
