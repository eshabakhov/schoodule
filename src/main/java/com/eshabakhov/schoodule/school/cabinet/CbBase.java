/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.cabinet;

import com.eshabakhov.schoodule.school.Cabinet;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Postgres implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
public final class CbBase implements Cabinet {

    /** Cabinet id. */
    private final long cid;

    /** Cabinet name. */
    private final String cname;

    public CbBase(final String cname) {
        this(Long.MIN_VALUE, cname);
    }

    public CbBase(final long cid, final String cname) {
        this.cid = cid;
        this.cname = cname;
    }

    @Override
    public Long uid() {
        return this.cid;
    }

    @Override
    public String name() {
        return this.cname;
    }

    @Override
    public Cabinet renamed(final String name) {
        return new CbBase(this.cid, name);
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.cid)
            .put("name", this.cname);
    }
}
