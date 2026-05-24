/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schoolclass;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Postgres implementation of {@link SchoolClass}.
 *
 * @since 0.0.1
 */
public final class ScBase implements SchoolClass {

    /** School class id. */
    private final long sid;

    /** School class grade. */
    private final Integer grd;

    /** School class litera. */
    private final String ltr;

    public ScBase(final long sid, final Integer grd, final String ltr) {
        this.sid = sid;
        this.grd = grd;
        this.ltr = ltr;
    }

    @Override
    public Long uid() {
        return this.sid;
    }

    @Override
    public String name() {
        return String.format("%d%s", this.grd, this.ltr);
    }

    @Override
    public Integer grade() {
        return this.grd;
    }

    @Override
    public String litera() {
        return this.ltr;
    }

    @Override
    public SchoolClass regraded(final Integer grade) {
        return new ScBase(this.sid, grade, this.ltr);
    }

    @Override
    public SchoolClass reliterated(final String litera) {
        return new ScBase(this.sid, this.grd, litera);
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.sid)
            .put("name", String.format("%d%s", this.grd, this.ltr))
            .put("grade", this.grd)
            .put("litera", this.ltr);
    }
}
