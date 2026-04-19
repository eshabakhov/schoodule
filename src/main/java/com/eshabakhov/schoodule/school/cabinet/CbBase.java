/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.cabinet;

import com.eshabakhov.schoodule.school.Cabinet;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Postgres implementation of {@link Cabinet}.
 *
 * @since 0.0.1
 */
public final class CbBase implements Cabinet {

    /** Cabinet. */
    private final Cabinet cabinet;

    public CbBase(final Cabinet cabinet) {
        this.cabinet = cabinet;
    }

    @Override
    public Long uid() {
        return this.cabinet.uid();
    }

    @Override
    public String name() {
        return this.cabinet.name();
    }

    @Override
    public ObjectNode json() {
        return this.cabinet.json();
    }
}
