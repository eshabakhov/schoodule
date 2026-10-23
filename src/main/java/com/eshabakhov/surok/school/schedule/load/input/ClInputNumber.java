/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.load.ClassLoadInput;
import java.util.Objects;

/**
 * Positive identifier or weekly hours in a load input.
 *
 * @since 0.0.1
 */
public final class ClInputNumber implements ClassLoadInput {

    /** Origin input. */
    private final ClassLoadInput origin;

    /** Field name. */
    private final String name;

    /** Requested value. */
    private final String value;

    /**
     * New numeric field.
     *
     * @param origin Origin input
     * @param name Field name
     * @param value Requested value
     */
    public ClInputNumber(final ClassLoadInput origin, final String name, final String value) {
        this.origin = origin;
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        final long parsed;
        try {
            parsed = Long.parseLong(Objects.toString(this.value, "").trim());
        } catch (final NumberFormatException exception) {
            throw new LoadInputException(
                String.format("Positive integer field '%s' is required", this.name), exception
            );
        }
        if (parsed <= 0 || "hoursPerWeek".equals(this.name) && parsed > Integer.MAX_VALUE) {
            throw new LoadInputException(
                String.format("Field '%s' is outside the supported range", this.name)
            );
        }
        return this.origin.print(media).with(this.name, parsed);
    }
}
