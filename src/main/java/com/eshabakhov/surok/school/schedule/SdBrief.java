/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Printable;

/**
 * Schedule navigation item.
 *
 * @since 0.0.1
 */
public final class SdBrief implements Printable {

    /** Selected schedule. */
    private final Schedule origin;

    /**
     * New schedule navigation item.
     *
     * @param origin Selected schedule
     */
    public SdBrief(final Schedule origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media).include("id", "name");
    }
}
