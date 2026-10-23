/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Printable;

/**
 * A subject available in the load form.
 *
 * @since 0.0.1
 */
public final class ClSubject implements Printable {

    /** Identifier. */
    private final Long id;

    /** Display name. */
    private final String name;

    /**
     * New subject option.
     *
     * @param id Identifier
     * @param name Display name
     */
    public ClSubject(final Long id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("id", this.id).with("name", this.name);
    }
}
