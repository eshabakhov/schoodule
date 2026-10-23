/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.input;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.schedule.ScheduleInput;

/**
 * Empty school schedule input.
 *
 * @since 0.0.1
 */
public final class SdInput implements ScheduleInput {

    /**
     * New empty input.
     */
    public SdInput() {
        // Intentionally empty.
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media;
    }
}
