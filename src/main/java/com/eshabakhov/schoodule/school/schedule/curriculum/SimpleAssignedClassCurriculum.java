/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.curriculum;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple implementation of {@link ClassCurriculum}.
 *
 * @since 0.0.1
 */
public final class SimpleAssignedClassCurriculum implements AssignedClassCurriculum {

    /** Curriculum ID. */
    private final ClassCurriculum curriculum;

    /** School class. */
    private final Integer assigned;

    public SimpleAssignedClassCurriculum(
        final ClassCurriculum curriculum,
        final Integer assigned
    ) {
        this.curriculum = curriculum;
        this.assigned = assigned;
    }

    @Override
    public Long uid() {
        return this.curriculum.uid();
    }

    @Override
    public SchoolClass schoolClass() {
        return this.curriculum.schoolClass();
    }

    @Override
    public Subject subject() {
        return this.curriculum.subject();
    }

    @Override
    public Integer hoursPerWeek() {
        return this.curriculum.hoursPerWeek();
    }

    @Override
    public AssignedClassCurriculum assignedCurriculum() {
        return this;
    }

    @Override
    public ObjectNode json() {
        final var node = this.curriculum.json();
        node.put("assignedHours", this.assignedHours());
        return node;
    }

    @Override
    public Integer assignedHours() {
        final Integer result;
        if (this.assigned == null) {
            result = 0;
        } else {
            result = this.assigned;
        }
        return result;
    }
}
