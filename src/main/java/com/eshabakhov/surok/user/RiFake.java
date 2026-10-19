/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

/**
 * In-memory user reference information.
 *
 * @since 0.0.1
 */
public final class RiFake implements ReferenceInformation {

    /** School identity. */
    private final Long school;

    /** Corporate account marker. */
    private final Boolean corporate;

    /** Account availability marker. */
    private final Boolean available;

    /**
     * New in-memory reference information.
     *
     * @param school School identity
     * @param corporate Corporate account marker
     * @param available Account availability marker
     */
    public RiFake(
        final Long school,
        final Boolean corporate,
        final Boolean available
    ) {
        this.school = school;
        this.corporate = corporate;
        this.available = available;
    }

    @Override
    public Long school() {
        return this.school;
    }

    @Override
    public Boolean corporate() {
        return this.corporate;
    }

    @Override
    public Boolean alive() {
        return this.available;
    }
}
