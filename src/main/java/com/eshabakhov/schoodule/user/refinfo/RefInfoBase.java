/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.refinfo;

import com.eshabakhov.schoodule.user.ReferenceInformation;

/**
 * Base implementation of {@link ReferenceInformation}.
 *
 * @since 0.0.1
 */
public final class RefInfoBase implements ReferenceInformation {

    /** School id. */
    private final Long sid;

    /** Corporate user. */
    private final Boolean corp;

    /** Alive user. */
    private final Boolean alv;

    public RefInfoBase(final Long school, final Boolean corporate, final Boolean alive) {
        this.sid = school;
        this.corp = corporate;
        this.alv = alive;
    }

    @Override
    public Long school() {
        return this.sid;
    }

    @Override
    public Boolean corporate() {
        return this.corp;
    }

    @Override
    public Boolean alive() {
        return this.alv;
    }
}
