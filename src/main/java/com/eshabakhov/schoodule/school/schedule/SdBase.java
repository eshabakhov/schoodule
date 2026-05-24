/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.eshabakhov.schoodule.school.Schedule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base implementation of {@link Schedule}.
 *
 * @since 0.0.1
 */
public final class SdBase implements Schedule {

    /** Schedule id. */
    private final long sid;

    /** Schedule name. */
    private final String sname;

    public SdBase(final String sname) {
        this(Long.MIN_VALUE, sname);
    }

    public SdBase(final long sid, final String sname) {
        this.sid = sid;
        this.sname = sname;
    }

    @Override
    public Long uid() {
        return this.sid;
    }

    @Override
    public String name() {
        return this.sname;
    }

    @Override
    public Schedule renamed(final String name) {
        return new SdBase(this.sid, name);
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.sid)
            .put("name", this.sname);
    }

    @Override
    public ClassCurriculums curriculums() {
        throw new UnsupportedOperationException(
            "ClassCurriculums are infrastructure-dependent"
        );
    }
}
