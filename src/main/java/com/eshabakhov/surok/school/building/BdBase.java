/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.cabinet.Cabinets;
import com.eshabakhov.surok.school.building.input.MdBdInput;

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

    /**
     * New building without an ID.
     *
     * @param bname Building name
     * @since 0.0.1
     */
    public BdBase(final String bname) {
        this(Long.MIN_VALUE, bname);
    }

    /**
     * New building.
     *
     * @param bid Building ID
     * @param bname Building name
     * @since 0.0.1
     */
    public BdBase(final long bid, final String bname) {
        this.bid = bid;
        this.bname = bname;
    }

    @Override
    public Long uid() {
        return this.bid;
    }

    @Override
    public Building revise(final BuildingInput input) throws Exception {
        return new BdBase(this.bid, input.print(new MdBdInput()).print().get("name"));
    }

    @Override
    public Cabinets cabinets() {
        throw new UnsupportedOperationException("Cabinets are infrastructure-dependent");
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("id", this.bid).with("name", this.bname);
    }
}
