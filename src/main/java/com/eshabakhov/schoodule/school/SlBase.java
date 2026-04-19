/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.Users;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Basic implementation of {@link School}.
 *
 * @since 0.0.1
 */
@ToString(of = {"sid", "sname"})
@EqualsAndHashCode
public final class SlBase implements School {

    /** School id. */
    private final long sid;

    /** School name. */
    private final String sname;

    public SlBase(final String sname) {
        this(Long.MIN_VALUE, sname);
    }

    public SlBase(final long sid, final String sname) {
        this.sid = sid;
        this.sname = sname;
    }

    @Override
    public Long uid() {
        return this.sid;
    }

    @Override
    public String name() {
        return this.sname;
    }

    @Override
    public Users users() {
        return null;
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.sid)
            .put("name", this.sname);
    }

    @Override
    public Cabinets cabinets() {
        throw new UnsupportedOperationException(
            "Cabinets are infrastructure-dependent"
        );
    }

    @Override
    public Teachers teachers() {
        throw new UnsupportedOperationException(
            "Teachers are infrastructure-dependent"
        );
    }

    @Override
    public SchoolClasses schoolClasses() {
        throw new UnsupportedOperationException(
            "SchoolClasses are infrastructure-dependent"
        );
    }

    @Override
    public Subjects subjects() {
        throw new UnsupportedOperationException(
            "Subjects are infrastructure-dependent"
        );
    }

    @Override
    public Schedules schedules() {
        throw new UnsupportedOperationException(
            "Schedules are infrastructure-dependent"
        );
    }
}

