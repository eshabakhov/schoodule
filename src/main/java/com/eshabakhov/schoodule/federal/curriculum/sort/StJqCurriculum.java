/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.sort.StJooq;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import java.util.Optional;
import org.jooq.Field;
import org.jooq.SortField;

/**
 * Federal curriculum JOOQ sorting parameter.
 *
 * @since 0.0.1
 * @checkstyle CyclomaticComplexityCheck (100 lines)
 */
public final class StJqCurriculum implements StJooq {

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
    public StJqCurriculum(final Sort origin) {
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
            case null -> null;
            case "title" -> FederalCurriculum.FEDERAL_CURRICULUM.TITLE;
            case "level" -> FederalCurriculum.FEDERAL_CURRICULUM.EDUCATION_LEVEL;
            case "week" -> FederalCurriculum.FEDERAL_CURRICULUM.STUDY_WEEK_TYPE;
            case "version" -> FederalCurriculum.FEDERAL_CURRICULUM.VERSION;
            case "year" -> FederalCurriculum.FEDERAL_CURRICULUM.ACADEMIC_YEAR;
            default -> null;
        };
        return switch (this.origin.direction()) {
            case null -> null;
            case NONE -> null;
            case ASC -> Optional.ofNullable(field).map(Field::asc).orElse(null);
            case DESC -> Optional.ofNullable(field).map(Field::desc).orElse(null);
        };
    }
}
