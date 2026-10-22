/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.page.ResponsePageableList;
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
 * @checkstyle ProhibitFieldsInTestClassesCheck (1000 lines)
 */
@Testcontainers
@SpringBootTest
@SuppressWarnings({"PMD.UnusedPrivateMethod", "UnusedMethod"})
final class SlsPostgresTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("surok")
            .withUsername("test")
            .withPassword("test");

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    @Autowired
    SlsPostgresTest(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @AfterEach
    void truncateSchool() {
        this.ctx.resultQuery(
            """
                TRUNCATE TABLE public.school CASCADE
                """
        ).execute();
    }

    @Test
    void createsSchool() throws Exception {
        Assertions.assertEquals(
            new SlsPostgres(this.ctx).create("Awesome school"),
            new SlPostgres(
                this.ctx,
                this.ctx.resultQuery(
                    """
                        SELECT * FROM public.school
                        WHERE (school.name = ?) AND (school.is_deleted = ?)
                        """,
                    "Awesome school",
                    false
                ).fetchOne()
                    .get("id", Long.class)
            )
        );
    }

    /** Verifies that SQL-like Unicode names remain literal data. */
    @Test
    void preservesQuotedUnicodeSchoolNames() throws Exception {
        final String name = String.format(
            "Школа O'Brien '%s'; DROP TABLE public.school; --", java.util.UUID.randomUUID()
        );
        Assertions.assertEquals(
            name,
            new SlsPostgres(this.ctx).create(name).name(),
            "School creation did not preserve the SQL-like name"
        );
    }

    /** Verifies that name lookup binds quoted values. */
    @Test
    void findsSchoolsUsingBoundNames() throws Exception {
        final String name = String.format(
            "Лицей '%s' OR '1'='1", java.util.UUID.randomUUID()
        );
        Assertions.assertEquals(
            new SlsPostgres(this.ctx).create(name).uid(),
            new SlsPostgres(this.ctx).school(name).uid(),
            "School lookup did not bind the supplied name"
        );
    }

    @Test
    void findsSchoolById() throws Exception {
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            1L,
            "Boring school",
            false
        ).execute();
        Assertions.assertEquals(
            new SlPostgres(
                this.ctx,
                1L
            ),
            new SlsPostgres(this.ctx).school(1L)
        );
    }

    @Test
    void findsSchoolByName() throws Exception {
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            1L,
            "Cool school",
            false
        ).execute();
        Assertions.assertEquals(
            new SlPostgres(
                this.ctx,
                1L
            ),
            new SlsPostgres(this.ctx).school("Cool school")
        );
    }

    @Test
    void findsSchools() throws Exception {
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            1L,
            "Big school",
            false
        ).execute();
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            2L,
            "Small school",
            false
        ).execute();
        Assertions.assertEquals(
            new ResponsePageableList<>(
                List.of(
                    new SlPostgres(
                        this.ctx,
                        1L
                    ),
                    new SlPostgres(
                        this.ctx,
                        2L
                    )
                ),
                2,
                new PageRequest(10, 1)
            ),
            new SlsPostgres(this.ctx).schools(
                DSL.trueCondition(),
                new PageRequest(10, 1)
            )
        );
    }

    @Test
    void updatesSchool() throws Exception {
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            1L,
            "Math school",
            false
        ).execute();
        Assertions.assertEquals(
            "Simple school",
            new SlsPostgres(this.ctx)
                .school(1L)
                .renamed("Simple school")
                .name()
        );
    }

    @Test
    void removesSchool() throws Exception {
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            1L,
            "Bad school",
            false
        ).execute();
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name, is_deleted)
                VALUES (?, ?, ?)
                """,
            2L,
            "Good school",
            false
        ).execute();
        new SlsPostgres(this.ctx).remove(1L);
        Assertions.assertEquals(
            new ResponsePageableList<>(
                List.of(
                    new SlPostgres(
                        this.ctx,
                        2L
                    )
                ),
                1,
                new PageRequest(10, 1)
            ),
            new SlsPostgres(this.ctx).schools(
                DSL.condition(
                    """
                        school.is_deleted = ?
                        """,
                    false
                ),
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
