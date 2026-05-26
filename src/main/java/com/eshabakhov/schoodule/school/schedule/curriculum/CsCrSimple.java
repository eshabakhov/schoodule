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
public final class CsCrSimple implements ClassCurriculum {

    /** Curriculum ID. */
    private final Long cid;

    /** School class. */
    private final SchoolClass cls;

    /** Subject. */
    private final Subject sbj;

    /** School class. */
    private final Integer hrs;

    // @checkstyle ParameterNumberCheck (2 lines)
    public CsCrSimple(
        final Long cid,
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) {
        this.cid = cid;
        this.cls = clazz;
        this.sbj = subject;
        this.hrs = hours;
    }

    @Override
    public Long uid() {
        return this.cid;
    }

    @Override
    public SchoolClass schoolClass() {
        return this.cls;
    }

    @Override
    public Subject subject() {
        return this.sbj;
    }

    @Override
    public Integer hoursPerWeek() {
        return this.hrs;
    }

    @Override
    public ClassCurriculum teach(final Subject subject) {
        return new CsCrSimple(this.cid, this.cls, subject, this.hrs);
    }

    @Override
    public ClassCurriculum target(final SchoolClass clazz) {
        return new CsCrSimple(this.cid, clazz, this.sbj, this.hrs);
    }

    @Override
    public ClassCurriculum allocate(final Integer hours) {
        return new CsCrSimple(this.cid, this.cls, this.sbj, hours);
    }

    @Override
    public ObjectNode json() {
        final var node = JsonNodeFactory.instance.objectNode();
        node.put("id", this.cid);
        node.set("schoolClass", this.cls.json());
        node.set("subject", this.sbj.json());
        node.put("hoursPerWeek", this.hrs);
        return node;
    }
}
