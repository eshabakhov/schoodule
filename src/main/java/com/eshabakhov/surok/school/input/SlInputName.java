/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.SchoolInput;

/**
 * School input with a name.
 *
 * @since 0.0.1
 */
public final class SlInputName implements SchoolInput {

    /** Origin input. */
    private final SchoolInput origin;

    /** School name. */
    private final String name;

    /**
     * New name input.
     *
     * @param origin Origin input
     * @param name School name
     */
    public SlInputName(final SchoolInput origin, final String name) {
        this.origin = origin;
        this.name = name;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        if (this.name == null || this.name.isBlank()) {
            throw new SchoolInputException(
                "School name is required"
            );
        }
        return this.origin.print(media).with("name", this.name.trim());
    }
}
