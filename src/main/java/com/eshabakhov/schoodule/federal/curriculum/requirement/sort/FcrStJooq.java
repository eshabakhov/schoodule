/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.sort.StJooq;
import java.util.Optional;
import org.jooq.Field;
import org.jooq.SortField;

/**
 * Federal curriculum requirement JOOQ sorting parameter.
 *
 * @since 0.0.1
 */
public final class FcrStJooq implements StJooq {

    /**
     * JOOQ Table for FederalCurriculumRequirement.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

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
    public FcrStJooq(final Sort origin) {
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
            case "grade" -> FcrStJooq.REQUIREMENT.GRADE;
            case "subject" -> FcrStJooq.REQUIREMENT.SUBJECT_NAME;
            case "hours" -> FcrStJooq.REQUIREMENT.WEEKLY_HOURS;
            case "part" -> FcrStJooq.REQUIREMENT.PART_TYPE;
            default -> null;
        };
        return switch (this.origin.direction()) {
            case NONE -> null;
            case ASC -> Optional.ofNullable(field).map(Field::asc).orElse(null);
            case DESC -> Optional.ofNullable(field).map(Field::desc).orElse(null);
        };
    }
}
