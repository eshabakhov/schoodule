/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInputHours;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInputPart;
import com.eshabakhov.schoodule.federal.curriculum.requirement.input.RqInputSubject;
import com.eshabakhov.schoodule.media.MdJson;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.jooq.exception.NoDataFoundException;
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
 * Test for {@link RqPostgres}.
 *
 * @since 0.0.1
 * @checkstyle ProhibitFieldsInTestClassesCheck (1000 lines)
 */
@Testcontainers
@SpringBootTest
@SuppressWarnings({"PMD.UnusedPrivateMethod", "UnusedMethod"})
final class RqPostgresTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("schoodule")
            .withUsername("test")
            .withPassword("test");

    /** JOOQ context. */
    private final DSLContext ctx;

    @Autowired
    RqPostgresTest(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @BeforeEach
    void insertRequirement() {
        this.ctx.insertInto(FederalCurriculum.FEDERAL_CURRICULUM)
            .set(FederalCurriculum.FEDERAL_CURRICULUM.ID, 1L)
            .set(FederalCurriculum.FEDERAL_CURRICULUM.TITLE, "Plan").set(
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
        this.ctx.insertInto(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.ID,
            7L
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .FEDERAL_CURRICULUM_ID,
            1L
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.GRADE,
            1
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .SUBJECT_NAME,
            "Mathematics"
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .WEEKLY_HOURS,
            1
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .PART_TYPE,
            CurriculumPartType.MANDATORY
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .IS_DELETED,
            false
        ).execute();
    }

    @AfterEach
    void truncateCurriculums() {
        this.ctx.truncate(FederalCurriculum.FEDERAL_CURRICULUM)
            .restartIdentity().cascade().execute();
    }

    @Test
    void exposesIdentity() {
        MatcherAssert.assertThat(
            "Postgres requirement must expose its identity",
            new RqPostgres(this.ctx, 7L).uid(),
            Matchers.is(7L)
        );
    }

    @Test
    void printsRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Postgres requirement must print persisted fields",
            new RqPostgres(this.ctx, 7L).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 7L)
                    .put("grade", 1)
                    .put("subjectName", "Mathematics")
                    .put("weeklyHours", 1)
                    .put("partType", "MANDATORY")
            )
        );
    }

    @Test
    void revisesRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Revision must preserve identity and update every field",
            new RqPostgres(this.ctx, 7L).revise(
                new RqInputPart(
                    new RqInputHours(
                        new RqInputSubject(
                            new RqInputGrade(new RqInput(), "11"),
                            "Physics"
                        ),
                        "2"
                    ),
                    "OPTIONAL"
                )
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 7L)
                    .put("grade", 11)
                    .put("subjectName", "Physics")
                    .put("weeklyHours", 2)
                    .put("partType", "OPTIONAL")
            )
        );
    }

    @Test
    void returnsNullIdentityAfterRevisingMissingRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Revision of a missing requirement must expose no identity",
            new RqPostgres(this.ctx, 99L).revise(
                new RqInputPart(
                    new RqInputHours(
                        new RqInputSubject(
                            new RqInputGrade(new RqInput(), "5"),
                            "History"
                        ),
                        "2"
                    ),
                    "MANDATORY"
                )
            ).uid(),
            Matchers.nullValue()
        );
    }

    @Test
    void rejectsPrintingMissingRequirement() {
        Assertions.assertThrows(
            NoDataFoundException.class,
            () -> new RqPostgres(this.ctx, 99L).print(new MdJson())
        );
    }

    @Test
    void rejectsNullRevisionInput() {
        Assertions.assertThrows(
            NullPointerException.class,
            () -> new RqPostgres(this.ctx, 7L).revise(null)
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
            RqPostgresTest.POSTGRES::getJdbcUrl
        );
        registry.add(
            "spring.datasource.username",
            RqPostgresTest.POSTGRES::getUsername
        );
        registry.add(
            "spring.datasource.password",
            RqPostgresTest.POSTGRES::getPassword
        );
        registry.add("spring.liquibase.enabled", () -> true);
    }
}
