/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.teacher;

import com.eshabakhov.schoodule.school.Teacher;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple implementation of {@link Teacher}.
 *
 * @since 0.0.1
 */
public final class ThBase implements Teacher {

    /** Teacher ID. */
    private final long tid;

    /** Teacher name. */
    private final String tname;

    public ThBase(final String tname) {
        this(Long.MIN_VALUE, tname);
    }

    public ThBase(final long tid, final String tname) {
        this.tid = tid;
        this.tname = tname;
    }

    @Override
    public Long uid() {
        return this.tid;
    }

    @Override
    public String name() {
        return this.tname;
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.tid)
            .put("name", this.tname);
    }
}
