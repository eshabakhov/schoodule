/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Basic implementation of {@link FederalCurriculumRequirement}.
 *
 * @since 0.0.1
 */
@ToString(of = {"rid", "grd", "subj"})
@EqualsAndHashCode
@SuppressWarnings("PMD.TooManyMethods")
public final class FcrBase implements FederalCurriculumRequirement {

    /** Federal curriculum requirement id. */
    private final long rid;

    /** Parent federal curriculum. */
    private final FederalCurriculum cur;

    /** Grade. */
    private final Integer grd;

    /** Subject name. */
    private final String subj;

    /** Weekly hours. */
    private final Integer hrs;

    /** Curriculum part type. */
    private final PartType prt;

    // @checkstyle ParameterNumberCheck (2 lines)
    public FcrBase(
        final long rid,
        final FederalCurriculum curriculum,
        final Integer grade,
        final String subject,
        final Integer hours,
        final PartType part
    ) {
        this.rid = rid;
        this.cur = curriculum;
        this.grd = grade;
        this.subj = subject;
        this.hrs = hours;
        this.prt = part;
    }

    @Override
    public Long uid() {
        return this.rid;
    }

    @Override
    public FederalCurriculum curriculum() {
        return this.cur;
    }

    @Override
    public Integer grade() {
        return this.grd;
    }

    @Override
    public String subjectName() {
        return this.subj;
    }

    @Override
    public Integer weeklyHours() {
        return this.hrs;
    }

    @Override
    public PartType partType() {
        return this.prt;
    }

    @Override
    public FederalCurriculumRequirement regraded(final Integer grade) {
        return new FcrBase(this.rid, this.cur, grade, this.subj, this.hrs, this.prt);
    }

    @Override
    public FederalCurriculumRequirement resubjected(final String subject) {
        return new FcrBase(this.rid, this.cur, this.grd, subject, this.hrs, this.prt);
    }

    @Override
    public FederalCurriculumRequirement reweekled(final Integer hours) {
        return new FcrBase(this.rid, this.cur, this.grd, this.subj, hours, this.prt);
    }

    @Override
    public FederalCurriculumRequirement reparted(final PartType part) {
        return new FcrBase(this.rid, this.cur, this.grd, this.subj, this.hrs, part);
    }

    @Override
    public ObjectNode json() {
        final ObjectNode node = JsonNodeFactory.instance.objectNode()
            .put("id", this.rid)
            .put("grade", this.grd)
            .put("subjectName", this.subj)
            .put("weeklyHours", this.hrs)
            .put("partType", this.prt.name());
        if (this.cur != null) {
            node.set("curriculum", this.cur.json());
        }
        return node;
    }
}
