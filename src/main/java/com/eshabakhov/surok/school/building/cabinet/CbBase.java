/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.school.building.cabinet.input.MdCbInput;

/**
 * Base implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
public final class CbBase implements Cabinet {

    /** Cabinet id. */
    private final long cid;

    /** Cabinet name. */
    private final String cname;

    /**
     * New cabinet without an ID.
     *
     * @param cname Cabinet name
     * @since 0.0.1
     */
    public CbBase(final String cname) {
        this(Long.MIN_VALUE, cname);
    }

    /**
     * New cabinet.
     *
     * @param cid Cabinet ID
     * @param cname Cabinet name
     * @since 0.0.1
     */
    public CbBase(final long cid, final String cname) {
        this.cid = cid;
        this.cname = cname;
    }

    @Override
    public Long uid() {
        return this.cid;
    }

    @Override
    public Cabinet revise(final CabinetInput input) throws Exception {
        return new CbBase(this.cid, input.print(new MdCbInput()).print().get("name"));
    }

    @Override
    public <T> Media<T> print(final Media<T> media) {
        return media.with("id", this.cid).with("name", this.cname);
    }
}
