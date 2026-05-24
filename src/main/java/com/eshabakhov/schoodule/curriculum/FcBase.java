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
public final class FcBase implements FederalCurriculum {

    /** Federal curriculum id. */
    private final long fid;

    /** Title. */
    private final String ttl;

    /** Education level. */
    private final EducationLevel lvl;

    /** Study week type. */
    private final StudyWeek week;

    /** Version. */
    private final String ver;

    /** Academic year. */
    private final String year;

    /** Description. */
    private final String desc;

    // @checkstyle ParameterNumberCheck (2 lines)
    public FcBase(
        final String title,
        final EducationLevel level,
        final StudyWeek week,
        final String version,
        final String year,
        final String description
    ) {
        this(
            Long.MIN_VALUE,
            title,
            level,
            week,
            version,
            year,
            description
        );
    }

    // @checkstyle ParameterNumberCheck (2 lines)
    public FcBase(
        final long fid,
        final String title,
        final EducationLevel level,
        final StudyWeek week,
        final String version,
        final String year,
        final String description
    ) {
        this.fid = fid;
        this.ttl = title;
        this.lvl = level;
        this.week = week;
        this.ver = version;
        this.year = year;
        this.desc = description;
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
    public EducationLevel educationLevel() {
        return this.lvl;
    }

    @Override
    public StudyWeek studyWeekType() {
        return this.week;
    }

    @Override
    public String version() {
        return this.ver;
    }

    @Override
    public String academicYear() {
        return this.year;
    }

    @Override
    public String description() {
        return this.desc;
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
            .put("educationLevel", this.lvl.name())
            .put("studyWeekType", this.week.name())
            .put("version", this.ver)
            .put("academicYear", this.year)
            .put("description", this.desc);
    }
}
