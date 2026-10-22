/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.cabinet.Cabinets;

/**
 * Version 1 implementation of {@link Building}.
 *
 * @since 0.0.1
 */
public final class BdV1 implements Building {

    /**
     * Original building.
     */
    private final Building origin;

    /**
     * Creates a version 1 building.
     *
     * @param origin Original building
     */
    public BdV1(final Building origin) {
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
    public Building revise(final BuildingInput input) throws Exception {
        return this.origin.revise(input);
    }

    @Override
    public Cabinets cabinets() {
        return this.origin.cabinets();
    }
}
