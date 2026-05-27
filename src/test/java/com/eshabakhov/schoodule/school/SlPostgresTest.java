/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.school.building.BdsPostgres;
import com.eshabakhov.schoodule.school.schedule.SdsPostgres;
import com.eshabakhov.schoodule.school.schoolclass.ScsPostgres;
import com.eshabakhov.schoodule.school.subject.SbsPostgres;
import com.eshabakhov.schoodule.school.teacher.ThsPostgres;
import com.eshabakhov.schoodule.tables.School;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for {@link SlPostgres}.
 *
 * @since 0.0.1
 */
@Testcontainers
@SpringBootTest
@SuppressWarnings("PMD.UnusedPrivateMethod")
final class SlPostgresTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("schoodule")
            .withUsername("test")
            .withPassword("test");

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    @Autowired
    SlPostgresTest(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @AfterEach
    void truncateSchool() {
        this.ctx.truncate(School.SCHOOL).cascade().execute();
    }

    @Test
    void fetchBuildings() {
        this.ctx.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Cab school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new BdsPostgres(
                this.ctx,
                1L
            ),
            new SlPostgres(
                this.ctx,
                1L
            ).buildings()
        );
    }

    @Test
    void fetchTeachers() {
        this.ctx.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Tea school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new ThsPostgres(
                this.ctx,
                1L
            ),
            new SlPostgres(
                this.ctx,
                1L
            ).teachers()
        );
    }

    @Test
    void fetchSchoolClasses() {
        this.ctx.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Cla school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new ScsPostgres(
                this.ctx,
                1L
            ),
            new SlPostgres(
                this.ctx,
                1L
            ).schoolClasses()
        );
    }

    @Test
    void fetchSubjects() {
        this.ctx.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Sub school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new SbsPostgres(
                this.ctx,
                1L
            ),
            new SlPostgres(
                this.ctx,
                1L
            ).subjects()
        );
    }

    @Test
    void fetchSchedules() {
        this.ctx.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Sch school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new SdsPostgres(
                this.ctx,
                1L
            ),
            new SlPostgres(
                this.ctx,
                1L
            ).schedules()
        );
    }

    @DynamicPropertySource
    private static void properties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.url", SlPostgresTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SlPostgresTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SlPostgresTest.POSTGRES::getPassword);
        registry.add("spring.liquibase.enabled", () -> true);
    }
}
