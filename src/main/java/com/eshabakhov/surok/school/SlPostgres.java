/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.School;
import com.eshabakhov.surok.Users;
import com.eshabakhov.surok.school.building.BdsPostgres;
import com.eshabakhov.surok.school.schedule.SdsPostgres;
import com.eshabakhov.surok.school.schoolclass.ScsPostgres;
import com.eshabakhov.surok.school.subject.SbsPostgres;
import com.eshabakhov.surok.school.teacher.ThsPostgres;
import com.eshabakhov.surok.sql.ClasspathSql;
import com.eshabakhov.surok.user.UrsPostgres;
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

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /** School id. */
    private final Long sid;

    /**
     * New school.
     *
     * @param ctx Database context
     * @param sid School ID
     * @since 0.0.1
     */
    public SlPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public Long uid() {
        return this.ctx.resultQuery(
            new ClasspathSql("school/find-id.sql").text(),
            this.sid
        ).fetchOneInto(Long.class);
    }

    @Override
    public String name() {
        return this.ctx.resultQuery(
            new ClasspathSql("school/find-name.sql").text(),
            this.sid
        ).fetchOneInto(String.class);
    }

    @Override
    public School renamed(final String name) {
        return new SlPostgres(
            this.ctx,
            this.ctx.resultQuery(
                new ClasspathSql("school/rename.sql").text(),
                name,
                this.sid
            ).fetchOne("id", Long.class)
        );
    }

    @Override
    public Users users() {
        return new UrsPostgres(this.ctx, this.sid);
    }

    @Override
    public Buildings buildings() {
        return new BdsPostgres(this.ctx, this.sid);
    }

    @Override
    public Teachers teachers() {
        return new ThsPostgres(this.ctx, this.sid);
    }

    @Override
    public SchoolClasses schoolClasses() {
        return new ScsPostgres(this.ctx, this.sid);
    }

    @Override
    public Subjects subjects() {
        return new SbsPostgres(this.ctx, this.sid);
    }

    @Override
    public Schedules schedules() {
        return new SdsPostgres(this.ctx, this.sid);
    }

    @Override
    public ObjectNode json() {
        return this.ctx.resultQuery(
            new ClasspathSql("school/find-details.sql").text(),
            this.sid
        ).fetchOne(
            school ->
                JsonNodeFactory.instance.objectNode()
                    .put("id", school.get("id", Long.class))
                    .put("name", school.get("name", String.class))
        );
    }
}
