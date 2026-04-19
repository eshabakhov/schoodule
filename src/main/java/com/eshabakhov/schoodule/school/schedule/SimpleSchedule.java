/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.eshabakhov.schoodule.school.Schedule;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Postgres implementation of {@link Schedule}.
 *
 * @since 0.0.1
 */
public final class SimpleSchedule implements Schedule {

    /** Schedule. */
    private final Schedule schedule;

    public SimpleSchedule(final Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public Long uid() {
        return this.schedule.uid();
    }

    @Override
    public String name() {
        return this.schedule.name();
    }

    @Override
    public ObjectNode json() {
        return this.schedule.json();
    }

    @Override
    public ClassCurriculums curriculums() {
        return this.schedule.curriculums();
    }
}
