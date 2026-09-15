/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.sort.StJooq;
import com.eshabakhov.schoodule.sort.StsJooq;
import java.util.ArrayList;
import java.util.List;
import org.jooq.SortField;

/**
 * Federal curriculum requirement JOOQ sorting parameters.
 *
 * @since 0.0.1
 */
public final class FcrStsJooq implements StsJooq {

    /**
     * JOOQ Table for FederalCurriculumRequirement.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /**
     * Origin sorting parameters.
     */
    private final Sorts origin;

    /**
     * Ctor.
     *
     * @param origin Origin sorting parameters
     * @since 0.0.1
     */
    public FcrStsJooq(final Sorts origin) {
        this.origin = origin;
    }

    @Override
    public List<Sort> sorts() {
        return this.origin.sorts();
    }

    @Override
    public List<SortField<?>> fields() {
        final List<SortField<?>> fields = new ArrayList<>(this.origin.sorts().size());
        for (final String name : List.of("grade", "subject", "hours", "part")) {
            for (final Sort sort : this.origin.sorts()) {
                if (name.equals(sort.name())) {
                    final StJooq field = new FcrStJooq(sort);
                    if (field.field() != null) {
                        fields.add(field.field());
                    }
                }
            }
        }
        if (fields.isEmpty()) {
            fields.add(FcrStsJooq.REQUIREMENT.GRADE.asc());
            fields.add(FcrStsJooq.REQUIREMENT.SUBJECT_NAME.asc());
            fields.add(FcrStsJooq.REQUIREMENT.WEEKLY_HOURS.asc());
            fields.add(FcrStsJooq.REQUIREMENT.PART_TYPE.asc());
        }
        return fields;
    }
}
