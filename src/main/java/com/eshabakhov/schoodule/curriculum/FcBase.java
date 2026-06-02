/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Basic implementation of {@link FederalCurriculum}.
 *
 * @since 0.0.1
 */
@ToString(of = {"fid", "ttl"})
@EqualsAndHashCode
@SuppressWarnings("PMD.TooManyMethods")
public final class FcBase implements FederalCurriculum {

    /** Federal curriculum id. */
    private final long fid;

    /** Title. */
    private final String ttl;

    /** Education level. */
    private final Level lvl;

    /** Study week type. */
    private final Week wwk;

    /** Version. */
    private final String ver;

    /** Academic year. */
    private final String yar;

    // @checkstyle ParameterNumberCheck (2 lines)
    public FcBase(
        final long fid,
        final String title,
        final Level level,
        final Week week,
        final String version,
        final String year
    ) {
        this.fid = fid;
        this.ttl = title;
        this.lvl = level;
        this.wwk = week;
        this.ver = version;
        this.yar = year;
    }

    @Override
    public Long uid() {
        return this.fid;
    }

    @Override
    public String title() {
        return this.ttl;
    }

    @Override
    public Level level() {
        return this.lvl;
    }

    @Override
    public Week week() {
        return this.wwk;
    }

    @Override
    public String version() {
        return this.ver;
    }

    @Override
    public String year() {
        return this.yar;
    }

    @Override
    public String description() {
        return "";
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return new FcBase(this.fid, title, this.lvl, this.wwk, this.ver, this.yar);
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return new FcBase(this.fid, this.ttl, level, this.wwk, this.ver, this.yar);
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return new FcBase(this.fid, this.ttl, this.lvl, week, this.ver, this.yar);
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return new FcBase(this.fid, this.ttl, this.lvl, this.wwk, version, this.yar);
    }

    @Override
    public FederalCurriculum reyeared(final String year) {
        return new FcBase(this.fid, this.ttl, this.lvl, this.wwk, this.ver, year);
    }

    @Override
    public FederalCurriculum redescriptioned(final String description) {
        return new FcDescription(this, description);
    }

    @Override
    public FederalCurriculumRequirements requirements() {
        throw new UnsupportedOperationException(
            "FederalCurriculumRequirements are infrastructure-dependent"
        );
    }

    @Override
    public ObjectNode json() {
        return JsonNodeFactory.instance.objectNode()
            .put("id", this.fid)
            .put("title", this.ttl)
            .put("level", this.lvl.name())
            .put("week", this.wwk.name())
            .put("version", this.ver)
            .put("year", this.yar)
            .put("description", "");
    }
}
