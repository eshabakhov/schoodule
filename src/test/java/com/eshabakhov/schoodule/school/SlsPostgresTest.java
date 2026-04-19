/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import com.eshabakhov.schoodule.tables.School;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
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
 * Test for {@link SlsPostgres}.
 *
 * @since 0.0.1
 */
@Testcontainers
@SpringBootTest
@SuppressWarnings("PMD.UnusedPrivateMethod")
final class SlsPostgresTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("schoodule")
            .withUsername("test")
            .withPassword("test");

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    @Autowired
    SlsPostgresTest(final DSLContext datasource) {
        this.datasource = datasource;
    }

    @AfterEach
    void truncateSchool() {
        this.datasource.truncate(School.SCHOOL).cascade().execute();
    }

    @Test
    void createsSchool() throws Exception {
        final var created = new SlsPostgres(this.datasource).create("Awesome school");
        final var rec = this.datasource.selectFrom(School.SCHOOL)
            .where(School.SCHOOL.NAME.eq("Awesome school").and(School.SCHOOL.IS_DELETED.eq(false)))
            .fetchOne();
        Assertions.assertEquals(
            new SlPostgres(
                this.datasource,
                rec.getId()
            ),
            created
        );
    }

    @Test
    void findsSchoolById() throws Exception {
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Boring school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new SlPostgres(
                this.datasource,
                1L
            ),
            new SlsPostgres(this.datasource).find(1L)
        );
    }

    @Test
    void findsSchoolByName() throws Exception {
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Cool school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new SlPostgres(
                this.datasource,
                1L
            ),
            new SlsPostgres(this.datasource).find("Cool school")
        );
    }

    @Test
    void findsSchools() throws Exception {
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Big school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 2L)
            .set(School.SCHOOL.NAME, "Small school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new ResponsePageableList<>(
                List.of(
                    new SlPostgres(
                        this.datasource,
                        1L
                    ),
                    new SlPostgres(
                        this.datasource,
                        2L
                    )
                ),
                2,
                new PageRequest(10, 1)
            ),
            new SlsPostgres(this.datasource).list(
                DSL.trueCondition(),
                new PageRequest(10, 1)
            )
        );
    }

    @Test
    void updatesSchool() throws Exception {
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Math school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        Assertions.assertEquals(
            new SlBase(1L, "Simple school"),
            new SlsPostgres(this.datasource).put(new SlBase(1L, "Simple school"))
        );
    }

    @Test
    void removesSchool() throws Exception {
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 1L)
            .set(School.SCHOOL.NAME, "Bad school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        this.datasource.insertInto(School.SCHOOL)
            .set(School.SCHOOL.ID, 2L)
            .set(School.SCHOOL.NAME, "Good school")
            .set(School.SCHOOL.IS_DELETED, false)
            .execute();
        new SlsPostgres(this.datasource).remove(1L);
        Assertions.assertEquals(
            new ResponsePageableList<>(
                List.of(
                    new SlPostgres(
                        this.datasource,
                        2L
                    )
                ),
                1,
                new PageRequest(10, 1)
            ),
            new SlsPostgres(this.datasource).list(
                School.SCHOOL.IS_DELETED.eq(false),
                new PageRequest(10, 1)
            )
        );
    }

    @DynamicPropertySource
    private static void properties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.datasource.url", SlsPostgresTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", SlsPostgresTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", SlsPostgresTest.POSTGRES::getPassword);
        registry.add("spring.liquibase.enabled", () -> true);
    }
}
