/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.curriculum;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple implementation of {@link ClassCurriculum}.
 *
 * @since 0.0.1
 */
public final class SimpleClassCurriculum implements ClassCurriculum {

    /** Curriculum ID. */
    private final Long cid;

    /** School class. */
    private final SchoolClass clazz;

    /** Subject. */
    private final Subject subj;

    /** School class. */
    private final Integer hours;

    // @checkstyle ParameterNumberCheck (2 lines)
    public SimpleClassCurriculum(
        final Long cid,
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) {
        this.cid = cid;
        this.clazz = clazz;
        this.subj = subject;
        this.hours = hours;
    }

    @Override
    public Long uid() {
        return this.cid;
    }

    @Override
    public SchoolClass schoolClass() {
        return this.clazz;
    }

    @Override
    public Subject subject() {
        return this.subj;
    }

    @Override
    public Integer hoursPerWeek() {
        return this.hours;
    }

    @Override
    public AssignedClassCurriculum assignedCurriculum() {
        return null;
    }

    @Override
    public ObjectNode json() {
        final var node = JsonNodeFactory.instance.objectNode();
        node.put("id", this.cid);
        node.set("schoolClass", this.clazz.json());
        node.set("subject", this.subj.json());
        node.put("hoursPerWeek", this.hours);
        return node;
    }
}
