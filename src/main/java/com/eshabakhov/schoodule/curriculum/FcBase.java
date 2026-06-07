/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.Media;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Base in-memory implementation of {@link FederalCurriculum}.
 * Holds only the three fields that are always required: id, level, week.
 * All other fields are added via decorators.
 *
 * @since 0.0.1
 */
@ToString(of = "fid")
@EqualsAndHashCode(of = "fid")
public final class FcBase implements FederalCurriculum {

    /**
     * Federal curriculum id.
     */
    private final long fid;

    /**
     * Education level.
     */
    private final Level level;

    /**
     * Study week type.
     */
    private final Week week;

    /**
     * Creates a base curriculum.
     *
     * @param fid   Unique identifier
     * @param level Education level
     * @param week  Study week type
     */
    public FcBase(final long fid, final Level level, final Week week) {
        this.fid = fid;
        this.level = level;
        this.week = week;
    }

    @Override
    public Long uid() {
        return this.fid;
    }

    @Override
    public Media print(final Media media) {
        return media.with("id", this.fid)
            .with("level", this.level.name())
            .with("week", this.week.name());
    }

    @Override
    public FederalCurriculum retitled(final String title) {
        return new FcTitled(this, title);
    }

    @Override
    public FederalCurriculum releveled(final Level lvl) {
        return new FcBase(this.fid, lvl, this.week);
    }

    @Override
    public FederalCurriculum reweeked(final Week wkk) {
        return new FcBase(this.fid, this.level, wkk);
    }

    @Override
    public FederalCurriculum reversioned(final String version) {
        return new FcVersioned(this, version);
    }

    @Override
    public FederalCurriculum reyeared(final String year) {
        return new FcYeared(this, year);
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
}
