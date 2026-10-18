/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.sort.SortRequest;
import com.eshabakhov.schoodule.sort.SortsRequest;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link StsJqRequirement}.
 *
 * @since 0.0.1
 */
final class StsJqRequirementTest {

    @Test
    void delegatesSorts() {
        final List<Sort> sorts = List.of(
            new SortRequest("grade", Sort.Direction.ASC)
        );
        MatcherAssert.assertThat(
            "Decorated sorts must be returned without replacement",
            new StsJqRequirement(new SortsRequest(sorts)).sorts(),
            Matchers.sameInstance(sorts)
        );
    }

    @Test
    void usesCanonicalFieldOrder() {
        MatcherAssert.assertThat(
            "Requirement fields must follow their canonical priority",
            new StsJqRequirement(
                new SortsRequest(
                    List.of(
                        new SortRequest("part", Sort.Direction.DESC),
                        new SortRequest("hours", Sort.Direction.ASC),
                        new SortRequest("subject", Sort.Direction.DESC),
                        new SortRequest("grade", Sort.Direction.ASC)
                    )
                )
            ).fields(),
            Matchers.contains(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .GRADE.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .SUBJECT_NAME.desc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .WEEKLY_HOURS.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .PART_TYPE.desc()
            )
        );
    }

    @Test
    void keepsDuplicateFieldOrder() {
        MatcherAssert.assertThat(
            "Duplicate fields must preserve their request order",
            new StsJqRequirement(
                new SortsRequest(
                    List.of(
                        new SortRequest("grade", Sort.Direction.DESC),
                        new SortRequest("grade", Sort.Direction.ASC)
                    )
                )
            ).fields(),
            Matchers.contains(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .GRADE.desc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .GRADE.asc()
            )
        );
    }

    @Test
    void usesDefaultFieldsWithoutSorts() {
        MatcherAssert.assertThat(
            "Empty sorts must produce the default requirement order",
            new StsJqRequirement(
                new SortsRequest(List.of())
            ).fields(),
            Matchers.contains(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .GRADE.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .SUBJECT_NAME.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .WEEKLY_HOURS.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .PART_TYPE.asc()
            )
        );
    }

    @Test
    void usesDefaultFieldsForInapplicableSorts() {
        MatcherAssert.assertThat(
            "Inapplicable sorts must produce the default requirement order",
            new StsJqRequirement(
                new SortsRequest(
                    List.of(
                        new SortRequest(null, Sort.Direction.ASC),
                        new SortRequest("", Sort.Direction.DESC),
                        new SortRequest("unknown", Sort.Direction.ASC),
                        new SortRequest("grade", Sort.Direction.NONE),
                        new SortRequest("subject", null)
                    )
                )
            ).fields(),
            Matchers.contains(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .GRADE.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .SUBJECT_NAME.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .WEEKLY_HOURS.asc(),
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .PART_TYPE.asc()
            )
        );
    }

    @Test
    void rejectsNullSorts() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new StsJqRequirement(
                new SortsRequest(null)
            ).fields()
        );
    }
}
