/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.BuildingInput;

/**
 * School building input with a name.
 *
 * @since 0.0.1
 */
public final class BdInputName implements BuildingInput {

    /** Origin input. */
    private final BuildingInput origin;

    /** Building name. */
    private final String name;

    /**
     * New name input.
     *
     * @param origin Origin input
     * @param name Building name
     */
    public BdInputName(final BuildingInput origin, final String name) {
        this.origin = origin;
        this.name = name;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.name == null || this.name.isBlank()) {
            throw new BuildingInputException(
                "Building name is required"
            );
        }
        return this.origin.print(media).with("name", this.name.trim());
    }
}
