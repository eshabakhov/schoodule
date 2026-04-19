/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.subject;

import com.eshabakhov.schoodule.school.Subject;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Postgres implementation of {@link Subject}.
 *
 * @since 0.0.1
 */
public final class SbBase implements Subject {

    /** Subject id. */
    private final long sid;

    /** Subject name. */
    private final String subname;

    public SbBase(final String subname) {
        this(Long.MIN_VALUE, subname);
    }

    public SbBase(final long sid, final String subname) {
        this.sid = sid;
        this.subname = subname;
    }

    @Override
    public Long uid() {
        return this.sid;
    }

    @Override
    public String name() {
        return this.subname;
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.sid)
            .put("name", this.subname);
    }
}
