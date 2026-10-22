/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.sort;

import com.eshabakhov.surok.Sort;
import com.eshabakhov.surok.sort.StJooq;
import java.util.Optional;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;

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
            case "title" -> DSL.field(
                DSL.name("public", "federal_curriculum", "title"),
                String.class
            );
            case "level" -> DSL.field(
                DSL.name("public", "federal_curriculum", "education_level"),
                String.class
            );
            case "week" -> DSL.field(
                DSL.name("public", "federal_curriculum", "study_week_type"),
                String.class
            );
            case "version" -> DSL.field(
                DSL.name("public", "federal_curriculum", "version"),
                String.class
            );
            case "year" -> DSL.field(
                DSL.name("public", "federal_curriculum", "academic_year"),
                String.class
            );
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
