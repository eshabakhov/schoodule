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
public final class FcDescription implements FederalCurriculum {

    /** Federal curriculum origin. */
    private final FederalCurriculum origin;

    /** Description. */
    private final String desc;

    public FcDescription(final FederalCurriculum origin, final String desc) {
        this.origin = origin;
        this.desc = desc;
    }

    @Override
    public Long uid() {
        return this.origin.uid();
    }

    @Override
    public String title() {
        return this.origin.title();
    }

    @Override
    public Level level() {
        return this.origin.level();
    }

    @Override
    public Week week() {
        return this.origin.week();
    }

    @Override
    public String version() {
        return this.origin.version();
    }

    @Override
    public String year() {
        return this.origin.year();
    }

    @Override
    public String description() {
        return this.desc;
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return this.origin.retitled(title);
    }

    @Override
    public FederalCurriculum releveled(final Level level) {
        return this.origin.releveled(level);
    }

    @Override
    public FederalCurriculum reweeked(final Week week) {
        return this.origin.reweeked(week);
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return this.origin.reversioned(version);
    }

    @Override
    public FederalCurriculum reyeared(final String year) {
        return this.origin.reyeared(year);
    }

    @Override
    public FederalCurriculum redescriptioned(final String description) {
        return new FcDescription(this.origin, description);
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
            .put("id", this.origin.uid())
            .put("title", this.origin.title())
            .put("level", this.origin.level().name())
            .put("week", this.origin.week().name())
            .put("version", this.origin.version())
            .put("year", this.origin.year())
            .put("description", this.desc);
    }
}
