/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple implementation of {@link ClassLoad}.
 *
 * @since 0.0.1
 */
public final class ClassLoadSimple implements ClassLoad {

    /** Load ID. */
    private final Long lid;

    /** School class. */
    private final SchoolClass cls;

    /** Subject. */
    private final Subject sbj;

    /** School class. */
    private final Integer hrs;

    /**
     * New class load.
     *
     * @param lid Load ID
     * @param clazz School class
     * @param subject Subject
     * @param hours Weekly hours
     * @since 0.0.1
     */
    public ClassLoadSimple(
        final Long lid,
        final SchoolClass clazz,
        final Subject subject,
        final Integer hours
    ) {
        this.lid = lid;
        this.cls = clazz;
        this.sbj = subject;
        this.hrs = hours;
    }

    @Override
    public Long uid() {
        return this.lid;
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
    public ClassLoad teach(final Subject subject) {
        return new ClassLoadSimple(this.lid, this.cls, subject, this.hrs);
    }

    @Override
    public ClassLoad target(final SchoolClass clazz) {
        return new ClassLoadSimple(this.lid, clazz, this.sbj, this.hrs);
    }

    @Override
    public ClassLoad allocate(final Integer hours) {
        return new ClassLoadSimple(this.lid, this.cls, this.sbj, hours);
    }

    @Override
    public ObjectNode json() {
        final ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("id", this.lid);
        node.set("schoolClass", this.cls.json());
        node.set("subject", this.sbj.json());
        node.put("hoursPerWeek", this.hrs);
        return node;
    }
}
