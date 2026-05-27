/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building;

import com.eshabakhov.schoodule.school.Building;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base implementation of {@link Building}.
 *
 * @since 0.0.1
 */
public final class BdBase implements Building {

    /** Building id. */
    private final long bid;

    /** Building name. */
    private final String bname;

    public BdBase(final String bname) {
        this(Long.MIN_VALUE, bname);
    }

    public BdBase(final long bid, final String bname) {
        this.bid = bid;
        this.bname = bname;
    }

    @Override
    public Long uid() {
        return this.bid;
    }

    @Override
    public String name() {
        return this.bname;
    }

    @Override
    public Building renamed(final String name) {
        return new BdBase(this.bid, name);
    }

    @Override
    public Cabinets cabinets() {
        throw new UnsupportedOperationException("Cabinets are infrastructure-dependent");
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.bid)
            .put("name", this.bname);
    }
}
