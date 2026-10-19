/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Sort;
import com.eshabakhov.surok.enums.CurriculumPartType;
import com.eshabakhov.surok.enums.EducationLevelType;
import com.eshabakhov.surok.enums.StudyWeekType;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputHours;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputPart;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputSubject;
import com.eshabakhov.surok.filter.FilterRequest;
import com.eshabakhov.surok.filter.FiltersRequest;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.media.MdXml;
import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.sort.SortRequest;
import com.eshabakhov.surok.sort.SortsRequest;
import com.eshabakhov.surok.tables.FederalCurriculum;
import com.eshabakhov.surok.tables.FederalCurriculumRequirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.StreamSupport;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for {@link RqsPostgres}.
 *
 * @since 0.0.1
 * @checkstyle ProhibitFieldsInTestClassesCheck (1000 lines)
 */
@Testcontainers
@SpringBootTest
@SuppressWarnings({"PMD.UnusedPrivateMethod", "UnusedMethod"})
final class RqsPostgresTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("surok")
            .withUsername("test")
            .withPassword("test");

    /** JOOQ context. */
    private final DSLContext ctx;

    @Autowired
    RqsPostgresTest(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @BeforeEach
    void insertCurriculums() {
        for (long id = 1L; id <= 2L; ++id) {
            this.ctx.insertInto(FederalCurriculum.FEDERAL_CURRICULUM)
                .set(FederalCurriculum.FEDERAL_CURRICULUM.ID, id).set(
                    FederalCurriculum.FEDERAL_CURRICULUM.TITLE,
                    String.format("Plan %d", id)
                ).set(
                    FederalCurriculum.FEDERAL_CURRICULUM.EDUCATION_LEVEL,
                    EducationLevelType.BASIC
                ).set(
                    FederalCurriculum.FEDERAL_CURRICULUM.STUDY_WEEK_TYPE,
                    StudyWeekType.FIVE_DAYS
                )
                .set(FederalCurriculum.FEDERAL_CURRICULUM.VERSION, "1").set(
                    FederalCurriculum.FEDERAL_CURRICULUM.ACADEMIC_YEAR,
                    "2026/2027"
                )
                .set(FederalCurriculum.FEDERAL_CURRICULUM.IS_DELETED, false)
                .execute();
        }
    }

    @AfterEach
    void truncateCurriculums() {
        this.ctx.truncate(FederalCurriculum.FEDERAL_CURRICULUM)
            .restartIdentity().cascade().execute();
    }

    @Test
    void createsBoundaryRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Created requirement must contain persisted boundary values",
            new RqsPostgres(this.ctx, 1L).create(
                new RqInputPart(
                    new RqInputHours(
                        new RqInputSubject(
                            new RqInputGrade(new RqInput(), "1"),
                            "Literature"
                        ),
                        "1"
                    ),
                    "OPTIONAL"
                )
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 1L)
                    .put("grade", 1)
                    .put("subjectName", "Literature")
                    .put("weeklyHours", 1)
                    .put("partType", "OPTIONAL")
            )
        );
    }

    @Test
    void rejectsDuplicateRequirement() throws Exception {
        final RequirementInput input = new RqInputPart(
            new RqInputHours(
                new RqInputSubject(
                    new RqInputGrade(new RqInput(), "5"),
                    "Mathematics"
                ),
                "5"
            ),
            "MANDATORY"
        );
        new RqsPostgres(this.ctx, 1L).create(input);
        MatcherAssert.assertThat(
            "Transaction failure must retain the domain conflict",
            Assertions.assertThrows(
                DataAccessException.class,
                () -> new RqsPostgres(this.ctx, 1L).create(input)
            ).getCause(),
            Matchers.instanceOf(RequirementAlreadyExistsException.class)
        );
    }

    @Test
    void permitsSameRequirementInAnotherCurriculum() throws Exception {
        final RequirementInput input = new RqInputPart(
            new RqInputHours(
                new RqInputSubject(
                    new RqInputGrade(new RqInput(), "5"),
                    "Mathematics"
                ),
                "5"
            ),
            "MANDATORY"
        );
        new RqsPostgres(this.ctx, 1L).create(input);
        MatcherAssert.assertThat(
            "Another curriculum must accept the same requirement",
            new RqsPostgres(this.ctx, 2L).create(input).uid(),
            Matchers.notNullValue()
        );
    }

    @Test
    void rejectsNullCreationInput() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new RqsPostgres(this.ctx, 1L).create(null)
        );
    }

    // @checkstyle LambdaBodyLengthCheck (50 lines)
    @Test
    void rejectsEmptyInsertResult() {
        MatcherAssert.assertThat(
            "Empty insert result must retain the creation failure",
            Assertions.assertThrows(
                DataAccessException.class,
                () -> new RqsPostgres(
                    DSL.using(
                        new MockConnection(
                            context -> new MockResult[] {
                                new MockResult(
                                    0,
                                    DSL.using(SQLDialect.POSTGRES).newResult()
                                ),
                            }
                        ),
                        SQLDialect.POSTGRES
                    ),
                    1L
                ).create(
                    new RqInputPart(
                        new RqInputHours(
                            new RqInputSubject(
                                new RqInputGrade(new RqInput(), "5"),
                                "Mathematics"
                            ),
                            "5"
                        ),
                        "MANDATORY"
                    )
                )
            ).getCause(),
            Matchers.instanceOf(RequirementFailedCreateException.class)
        );
    }

    @Test
    void findsRequirement() throws Exception {
        this.ctx.insertInto(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.ID,
            17L
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .FEDERAL_CURRICULUM_ID,
            1L
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.GRADE,
            6
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .SUBJECT_NAME,
            "History"
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .WEEKLY_HOURS,
            2
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .PART_TYPE,
            CurriculumPartType.MANDATORY
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .IS_DELETED,
            false
        ).execute();
        MatcherAssert.assertThat(
            "Lookup must return requirement from the current curriculum",
            new RqsPostgres(this.ctx, 1L).requirement(17L).uid(),
            Matchers.is(17L)
        );
    }

    @Test
    void rejectsMissingRequirement() {
        Assertions.assertThrows(
            RequirementNotFoundException.class,
            () -> new RqsPostgres(this.ctx, 1L).requirement(404L)
        );
    }

    @Test
    void rejectsRequirementFromAnotherCurriculum() throws Exception {
        final Requirement created = new RqsPostgres(this.ctx, 2L).create(
            new RqInputPart(
                new RqInputHours(
                    new RqInputSubject(
                        new RqInputGrade(new RqInput(), "7"),
                        "Biology"
                    ),
                    "2"
                ),
                "MANDATORY"
            )
        );
        Assertions.assertThrows(
            RequirementNotFoundException.class,
            () -> new RqsPostgres(this.ctx, 1L)
                .requirement(created.uid())
        );
    }

    @Test
    void selectsFilteredSortedItems() throws Exception {
        this.ctx.insertInto(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.ID,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .FEDERAL_CURRICULUM_ID,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.GRADE,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .SUBJECT_NAME,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .WEEKLY_HOURS,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .PART_TYPE,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .IS_DELETED
        ).values(
            31L, 1L, 7, "Algebra", 3, CurriculumPartType.MANDATORY, false
        ).values(
            32L, 1L, 7, "Geometry", 2, CurriculumPartType.MANDATORY, false
        ).values(
            33L, 1L, 8, "Geometry", 2, CurriculumPartType.MANDATORY, false
        ).execute();
        MatcherAssert.assertThat(
            "Selection must apply filter, sorting and first-page limit",
            StreamSupport.stream(
                new RqsPostgres(this.ctx, 1L).selection(
                    new FiltersRequest(
                        new LinkedHashSet<>(
                            List.of(new FilterRequest("grade", "7"))
                        )
                    ),
                    new PageRequest(1, 1),
                    new SortsRequest(
                        List.of(
                            new SortRequest(
                                "subject", Sort.Direction.DESC
                            )
                        )
                    )
                ).iterate().spliterator(),
                false
            ).map(Requirement::uid).toList(),
            Matchers.contains(32L)
        );
    }

    @Test
    void reportsSelectedPage() throws Exception {
        this.ctx.insertInto(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.ID,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .FEDERAL_CURRICULUM_ID,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.GRADE,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .SUBJECT_NAME,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .WEEKLY_HOURS,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .PART_TYPE,
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .IS_DELETED
        ).values(
            31L, 1L, 7, "Algebra", 3, CurriculumPartType.MANDATORY, false
        ).values(
            32L, 1L, 7, "Geometry", 2, CurriculumPartType.MANDATORY, false
        ).values(
            33L, 1L, 8, "Geometry", 2, CurriculumPartType.MANDATORY, false
        ).execute();
        MatcherAssert.assertThat(
            "Selection must report pagination for all matching rows",
            new RqsPostgres(this.ctx, 1L).selection(
                new FiltersRequest(
                    new LinkedHashSet<>(
                        List.of(new FilterRequest("grade", "7"))
                    )
                ),
                new PageRequest(1, 1),
                new SortsRequest(
                    List.of(
                        new SortRequest("subject", Sort.Direction.DESC)
                    )
                )
            ).print(new MdJson()).print().path("page"),
            Matchers.is(
                new ObjectMapper().readTree(
                    """
                        {
                            "limit": 1,
                            "offset": 1,
                            "total": 2,
                            "totalPages": 2,
                            "hasNext": true,
                            "hasPrev": false
                        }
                    """
                )
            )
        );
    }

    @Test
    void iteratesOnlyActiveRequirementsFromCurrentCurriculum()
        throws Exception {
        final Requirement active = new RqsPostgres(this.ctx, 1L).create(
            new RqInputPart(
                new RqInputHours(
                    new RqInputSubject(
                        new RqInputGrade(new RqInput(), "9"),
                        "Chemistry"
                    ),
                    "2"
                ),
                "OPTIONAL"
            )
        );
        new RqsPostgres(this.ctx, 1L).remove(
            new RqsPostgres(this.ctx, 1L).create(
                new RqInputPart(
                    new RqInputHours(
                        new RqInputSubject(
                            new RqInputGrade(new RqInput(), "9"),
                            "Physics"
                        ),
                        "2"
                    ),
                    "OPTIONAL"
                )
            ).uid()
        );
        new RqsPostgres(this.ctx, 2L).create(
            new RqInputPart(
                new RqInputHours(
                    new RqInputSubject(
                        new RqInputGrade(new RqInput(), "9"),
                        "Geography"
                    ),
                    "2"
                ),
                "OPTIONAL"
            )
        );
        MatcherAssert.assertThat(
            "Iteration must exclude deleted and foreign requirements",
            StreamSupport.stream(
                new RqsPostgres(this.ctx, 1L).iterate().spliterator(),
                false
            ).map(Requirement::uid).toList(),
            Matchers.contains(active.uid())
        );
    }

    @Test
    void printsRequirementsAsXml() throws Exception {
        new RqsPostgres(this.ctx, 1L).create(
            new RqInputPart(
                new RqInputHours(
                    new RqInputSubject(
                        new RqInputGrade(new RqInput(), "10"),
                        "Informatics"
                    ),
                    "3"
                ),
                "MANDATORY"
            )
        );
        MatcherAssert.assertThat(
            "Requirements collection must print named XML elements",
            new RqsPostgres(this.ctx, 1L)
                .print(new MdXml("requirements")).print(),
            Matchers.allOf(
                Matchers.startsWith("<requirements><requirement>"),
                Matchers.containsString(
                    "<subjectName>Informatics</subjectName>"
                ),
                Matchers.endsWith("</requirement></requirements>")
            )
        );
    }

    @Test
    void removesRequirement() throws Exception {
        final Requirement created = new RqsPostgres(this.ctx, 1L).create(
            new RqInputPart(
                new RqInputHours(
                    new RqInputSubject(
                        new RqInputGrade(new RqInput(), "4"),
                        "Music"
                    ),
                    "1"
                ),
                "OPTIONAL"
            )
        );
        new RqsPostgres(this.ctx, 1L).remove(created.uid());
        MatcherAssert.assertThat(
            "Removal must mark the requirement as deleted",
            this.ctx.select(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .IS_DELETED
            ).from(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
            ).where(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.ID
                    .eq(created.uid())
            ).fetchOneInto(Boolean.class),
            Matchers.is(true)
        );
    }

    @Test
    void rejectsRemovingMissingRequirement() {
        Assertions.assertThrows(
            RequirementNotFoundException.class,
            () -> new RqsPostgres(this.ctx, 1L).remove(404L)
        );
    }

    @Test
    void rejectsRemovingRequirementTwice() throws Exception {
        final Requirement created = new RqsPostgres(this.ctx, 1L).create(
            new RqInputPart(
                new RqInputHours(
                    new RqInputSubject(
                        new RqInputGrade(new RqInput(), "3"),
                        "Art"
                    ),
                    "1"
                ),
                "OPTIONAL"
            )
        );
        new RqsPostgres(this.ctx, 1L).remove(created.uid());
        Assertions.assertThrows(
            RequirementNotFoundException.class,
            () -> new RqsPostgres(this.ctx, 1L).remove(created.uid())
        );
    }

    @DynamicPropertySource
    private static void properties(final DynamicPropertyRegistry registry) {
        registry.add(
            "spring.datasource.driver-class-name",
            () -> "org.postgresql.Driver"
        );
        registry.add(
            "spring.datasource.url",
            RqsPostgresTest.POSTGRES::getJdbcUrl
        );
        registry.add(
            "spring.datasource.username",
            RqsPostgresTest.POSTGRES::getUsername
        );
        registry.add(
            "spring.datasource.password",
            RqsPostgresTest.POSTGRES::getPassword
        );
        registry.add("spring.liquibase.enabled", () -> true);
    }
}
