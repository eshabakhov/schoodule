/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.Users;
import com.eshabakhov.schoodule.school.cabinet.CbsPostgres;
import com.eshabakhov.schoodule.school.schedule.SdsPostgres;
import com.eshabakhov.schoodule.school.schoolclass.ScsPostgres;
import com.eshabakhov.schoodule.school.subject.SbsPostgres;
import com.eshabakhov.schoodule.school.teacher.ThsPostgres;
import com.eshabakhov.schoodule.user.UrsPostgres;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jooq.DSLContext;

/**
 * Postgres implementation of {@link School}.
 *
 * @since 0.0.1
 */
@ToString(of = "origin")
@EqualsAndHashCode
public final class SlPostgres implements School {

    /** JOOQ Table for School. */
    private static final com.eshabakhov.schoodule.tables.School SCHOOL =
        com.eshabakhov.schoodule.tables.School.SCHOOL;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    /** School id. */
    private final Long sid;

    public SlPostgres(final DSLContext datasource, final Long sid) {
        this.datasource = datasource;
        this.sid = sid;
    }

    @Override
    public Long uid() {
        return this.datasource
            .select(SlPostgres.SCHOOL.ID)
            .from(SlPostgres.SCHOOL)
            .where(SlPostgres.SCHOOL.ID.eq(this.sid))
            .fetchOneInto(Long.class);
    }

    @Override
    public String name() {
        return this.datasource
            .select(SlPostgres.SCHOOL.NAME)
            .from(SlPostgres.SCHOOL)
            .where(SlPostgres.SCHOOL.ID.eq(this.sid))
            .fetchOneInto(String.class);
    }

    @Override
    public Users users() {
        return new UrsPostgres(this.datasource, this.sid);
    }

    @Override
    public ObjectNode json() {
        return this.datasource
            .select(SlPostgres.SCHOOL.ID, SlPostgres.SCHOOL.NAME)
            .from(SlPostgres.SCHOOL)
            .where(SlPostgres.SCHOOL.ID.eq(this.sid))
            .fetchOne(
                school ->
                    JsonNodeFactory.instance.objectNode()
                        .put("id", school.get(SlPostgres.SCHOOL.ID))
                        .put("name", school.get(SlPostgres.SCHOOL.NAME))
            );
    }

    @Override
    public Cabinets cabinets() {
        return new CbsPostgres(this.datasource, this.sid);
    }

    @Override
    public Teachers teachers() {
        return new ThsPostgres(this.datasource, this.sid);
    }

    @Override
    public SchoolClasses schoolClasses() {
        return new ScsPostgres(this.datasource, this.sid);
    }

    @Override
    public Subjects subjects() {
        return new SbsPostgres(this.datasource, this.sid);
    }

    @Override
    public Schedules schedules() {
        return new SdsPostgres(this.datasource, this.sid);
    }
}
