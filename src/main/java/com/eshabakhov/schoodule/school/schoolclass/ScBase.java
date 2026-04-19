/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schoolclass;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Postgres implementation of {@link SchoolClass}.
 *
 * @since 0.0.1
 */
public final class ScBase implements SchoolClass {

    /** School class. */
    private final SchoolClass clazz;

    public ScBase(final SchoolClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public Long uid() {
        return this.clazz.uid();
    }

    @Override
    public String name() {
        return this.clazz.name();
    }

    @Override
    public ObjectNode json() {
        return this.clazz.json();
    }
}
