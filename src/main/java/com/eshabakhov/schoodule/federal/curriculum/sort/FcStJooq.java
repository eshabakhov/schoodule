/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.sort.StJooq;
import java.util.Optional;
import org.jooq.Field;
import org.jooq.SortField;

/**
 * Federal curriculum JOOQ sorting parameter.
 *
 * @since 0.0.1
 */
public final class FcStJooq implements StJooq {

    /**
     * JOOQ Table for FederalCurriculum.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /**
     * Origin sorting parameter.
     */
    private final Sort origin;

    /**
     * Ctor.
     *
     * @param origin Origin sorting parameter
     * @since 0.0.1
     */
    public FcStJooq(final Sort origin) {
        this.origin = origin;
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    public Direction direction() {
        return this.origin.direction();
    }

    @Override
    public SortField<?> field() {
        final Field<?> field = switch (this.origin.name()) {
            case "title" -> FcStJooq.CURRICULUM.TITLE;
            case "level" -> FcStJooq.CURRICULUM.EDUCATION_LEVEL;
            case "week" -> FcStJooq.CURRICULUM.STUDY_WEEK_TYPE;
            case "version" -> FcStJooq.CURRICULUM.VERSION;
            case "year" -> FcStJooq.CURRICULUM.ACADEMIC_YEAR;
            default -> null;
        };
        return switch (this.origin.direction()) {
            case NONE -> null;
            case ASC -> Optional.ofNullable(field).map(Field::asc).orElse(null);
            case DESC -> Optional.ofNullable(field).map(Field::desc).orElse(null);
        };
    }
}
