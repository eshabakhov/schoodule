/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.filter;

import com.eshabakhov.schoodule.Filter;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.filter.FilterRequest;
import com.eshabakhov.schoodule.filter.FiltersRequest;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link FlsCdRequirement}.
 *
 * @since 0.1.0
 */
final class FlsCdRequirementTest {

    @Test
    void delegatesFilters() {
        final Set<Filter> filters = Set.of(
            new FilterRequest("grade", "5")
        );
        MatcherAssert.assertThat(
            "Decorated filters must be returned without replacement",
            new FlsCdRequirement(new FiltersRequest(filters)).filters(),
            Matchers.sameInstance(filters)
        );
    }

    @Test
    void acceptsEverythingWithoutFilters() {
        MatcherAssert.assertThat(
            "Empty filters must accept every requirement",
            new FlsCdRequirement(
                new FiltersRequest(Set.of())
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @Test
    void combinesCanonicalFilters() {
        MatcherAssert.assertThat(
            "Canonical filters must form one condition",
            new FlsCdRequirement(
                new FiltersRequest(
                    new LinkedHashSet<>(
                        List.of(
                            new FilterRequest("grade", "5"),
                            new FilterRequest("subject", "Mathematics"),
                            new FilterRequest("part", "MANDATORY")
                        )
                    )
                )
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .GRADE.eq(5)
                ).and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .SUBJECT_NAME.likeIgnoreCase("%Mathematics%")
                ).and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .PART_TYPE.eq(CurriculumPartType.MANDATORY)
                )
            )
        );
    }

    @Test
    void combinesAliasFilters() {
        MatcherAssert.assertThat(
            "Alias filters must form one condition",
            new FlsCdRequirement(
                new FiltersRequest(
                    new LinkedHashSet<>(
                        List.of(
                            new FilterRequest("subjectName", "History"),
                            new FilterRequest("partType", "OPTIONAL")
                        )
                    )
                )
            ).condition(),
            Matchers.is(
                DSL.trueCondition().and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .SUBJECT_NAME.likeIgnoreCase("%History%")
                ).and(
                    FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                        .PART_TYPE.eq(CurriculumPartType.OPTIONAL)
                )
            )
        );
    }

    @Test
    void ignoresUnknownFilter() {
        MatcherAssert.assertThat(
            "Unknown filter must not restrict requirements",
            new FlsCdRequirement(
                new FiltersRequest(
                    Set.of(new FilterRequest("unknown", "value"))
                )
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @Test
    void ignoresFilterWithoutName() {
        MatcherAssert.assertThat(
            "Nameless filter must not restrict requirements",
            new FlsCdRequirement(
                new FiltersRequest(
                    Set.of(new FilterRequest(null, "value"))
                )
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @Test
    void ignoresEmptyKnownFilters() {
        MatcherAssert.assertThat(
            "Empty known filters must not restrict requirements",
            new FlsCdRequirement(
                new FiltersRequest(
                    Set.of(
                        new FilterRequest("grade", ""),
                        new FilterRequest("subject", " "),
                        new FilterRequest("part", null)
                    )
                )
            ).condition(),
            Matchers.is(DSL.trueCondition())
        );
    }

    @Test
    void rejectsMalformedGrade() {
        Assertions.assertThrows(
            NumberFormatException.class,
            () -> new FlsCdRequirement(
                new FiltersRequest(
                    Set.of(new FilterRequest("grade", "fifth"))
                )
            ).condition()
        );
    }

    @Test
    void rejectsMalformedPart() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new FlsCdRequirement(
                new FiltersRequest(
                    Set.of(new FilterRequest("part", "UNKNOWN"))
                )
            ).condition()
        );
    }
}
