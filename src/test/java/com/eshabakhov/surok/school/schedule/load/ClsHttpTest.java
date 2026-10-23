/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.User;
import com.eshabakhov.surok.user.AuthUser;
import com.eshabakhov.surok.user.Roles;
import com.eshabakhov.surok.user.UrPostgres;
import java.util.List;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Unified teaching load HTTP contracts verified with MockMvc assertions.
 *
 * @since 0.0.1
 * @checkstyle ProhibitFieldsInTestClassesCheck (1000 lines)
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings({
    "PMD.UnusedPrivateMethod", "UnusedMethod",
    "PMD.UnitTestShouldIncludeAssert"
})
final class ClsHttpTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("surok")
            .withUsername("test")
            .withPassword("test");

    /** Database context. */
    private final DSLContext ctx;

    /** HTTP client with the application's security filters. */
    private final MockMvc mvc;

    @Autowired
    ClsHttpTest(final DSLContext ctx, final MockMvc mvc) {
        this.ctx = ctx;
        this.mvc = mvc;
    }

    @BeforeEach
    void insertLoads() {
        this.ctx.resultQuery(
            """
            INSERT INTO public.school (id, name) VALUES (1, 'Школа'), (2, 'Чужая школа')
            """
        ).execute();
        this.ctx.resultQuery(
            """
            INSERT INTO public.schedule (id, school_id, name, is_deleted)
            VALUES (100, 1, 'Год', false), (101, 1, 'Полугодие', false),
                (102, 1, 'Удалённое расписание', true), (200, 2, 'Чужой год', false)
            """
        ).execute();
        this.ctx.resultQuery(
            """
            INSERT INTO public.school_class (id, school_id, grade, litera, is_deleted)
            VALUES (10, 1, 2, 'А', false), (11, 1, 10, 'Б', false),
                (12, 1, 3, 'Удалён', true), (20, 2, 7, 'Чужой', false)
            """
        ).execute();
        this.ctx.resultQuery(
            """
            INSERT INTO public.subject (id, school_id, name, is_deleted)
            VALUES (30, 1, 'Математика', false), (31, 1, 'Физика', false),
                (32, 1, 'Удалённый предмет', true), (40, 2, 'Чужой предмет', false)
            """
        ).execute();
        this.ctx.resultQuery(
            """
            INSERT INTO public.class_curriculum
                (id, schedule_id, school_class_id, subject_id, hours_per_week)
            VALUES (50, 100, 10, 30, 5), (51, 100, 11, 31, 3),
                (52, 101, 10, 30, 2), (60, 200, 20, 40, 1)
            """
        ).execute();
    }

    @AfterEach
    void truncateSchools() {
        this.ctx.resultQuery(
            """
                TRUNCATE TABLE public.school RESTART IDENTITY CASCADE
                """
        ).execute();
    }

    @Test
    void requiresAuthentication() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads")
        ).andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    void printsOnlyRequestedPageAndSchedule() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .accept("application/vnd.surok.class-loads.v1+json")
                .param("limit", "1").param("offset", "2")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(51))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].schoolClassName").value("10Б"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.total").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.offset").value(2));
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .accept("application/vnd.surok.class-loads.v1+xml")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.xpath("count(/loads/load)").number(2.0));
    }

    @Test
    void negotiatesEntityFormats() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/50")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .accept("application/vnd.surok.class-load-v1+json")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.schoolClassId").value(10))
            .andExpect(MockMvcResultMatchers.jsonPath("$.subjectName").value("Математика"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hoursPerWeek").value(5));
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/50")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .accept("application/vnd.surok.class-load-v1+xml")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.xpath("/load/schoolClassName").string("2А"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "", "/", "/fragment", "/create", "/50", "/50/edit"
        }
    )
    void rendersXsltPages(final String suffix) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get(String.format("/schools/1/schedules/100/loads%s", suffix))
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().string(Matchers.not(Matchers.isEmptyString()))
        );
    }

    @Test
    void offersOnlyActiveChoicesInThisSchool() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/create")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().string(
                Matchers.allOf(
                    Matchers.containsString("name=\"_csrf\""),
                    Matchers.containsString("Математика"), Matchers.containsString("10Б"),
                    Matchers.not(Matchers.containsString("Чужой")),
                    Matchers.not(Matchers.containsString(">3Удалён</option>")),
                    Matchers.not(Matchers.containsString(">Удалённый предмет</option>")),
                    Matchers.containsString("/schools/1/schedules/100/loads")
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "", "/", "/create"
        }
    )
    void createsVersionedJson(final String suffix) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post(
                    String.format("/schools/1/schedules/100/loads%s", suffix)
                ), Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json")
                .accept("application/vnd.surok.class-load-v1+json")
                .content("{\"schoolClassId\":10,\"subjectId\":31,\"hoursPerWeek\":4}")
        ).andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.subjectName").value("Физика"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hoursPerWeek").value(4)).andExpect(
                MockMvcResultMatchers.header().string(
                    "Location", Matchers.startsWith("/schools/1/schedules/100/loads/")
                )
            );
    }

    @Test
    void createsFromProtectedFormAndNegotiatesXml() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads/create"),
                Roles.RoleEnum.DIRECTOR
            ).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept("application/vnd.surok.class-load-v1+xml")
                .param("schoolClassId", "10").param("subjectId", "31").param("hoursPerWeek", "4")
        ).andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.xpath("/load/subjectName").string("Физика"));
    }

    @Test
    void revisesEveryFieldFromJson() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json")
                .accept("application/vnd.surok.class-load-v1+json")
                .content("{\"schoolClassId\":11,\"subjectId\":30,\"hoursPerWeek\":7}")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(50))
            .andExpect(MockMvcResultMatchers.jsonPath("$.schoolClassName").value("10Б"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.subjectName").value("Математика"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hoursPerWeek").value(7));
    }

    @Test
    void revisesFromProtectedForm() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads/50/edit"),
                Roles.RoleEnum.DIRECTOR
            ).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("schoolClassId", "11").param("subjectId", "30").param("hoursPerWeek", "7")
        ).andExpect(MockMvcResultMatchers.status().isFound())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/schools/1/schedules/100/loads/50"));
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/50")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .accept("application/vnd.surok.class-load-v1+json")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.hoursPerWeek").value(7));
    }

    @Test
    void redirectsSuccessfulCreation() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads/create"),
                Roles.RoleEnum.DIRECTOR
            ).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("schoolClassId", "10").param("subjectId", "31").param("hoursPerWeek", "4")
        ).andExpect(MockMvcResultMatchers.status().isFound())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/schools/1/schedules/100/loads"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "application/json", "application/xml", "application/pdf", "invalid",
            "*/*;q=0", "application/vnd.surok.class-load-v2+json"
        }
    )
    void checksAcceptBeforeMutations(final String accept) throws Exception {
        for (final String suffix : List.of("/create", "/50/edit")) {
            this.mvc.perform(
                this.authorized(
                    MockMvcRequestBuilders.post(
                        String.format("/schools/1/schedules/100/loads%s", suffix)
                    ), Roles.RoleEnum.DIRECTOR
                ).contentType(MediaType.APPLICATION_FORM_URLENCODED).header("Accept", accept)
                    .param("schoolClassId", "11").param("subjectId", "30")
                    .param("hoursPerWeek", "9")
            ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        }
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json").header("Accept", accept)
                .content("{\"schoolClassId\":11,\"subjectId\":30,\"hoursPerWeek\":9}")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads"),
                Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json").header("Accept", accept)
                .content("{\"schoolClassId\":11,\"subjectId\":30,\"hoursPerWeek\":9}")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        this.unchanged();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "application/json", "application/xml",
            "application/vnd.surok.class-loads.v1+json"
        }
    )
    void rejectsUnsupportedContentTypes(final String type) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads/create"),
                Roles.RoleEnum.DIRECTOR
            ).contentType(type).accept("application/vnd.surok.class-load-v1+json")
                .content("{\"schoolClassId\":11,\"subjectId\":30,\"hoursPerWeek\":9}")
        ).andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());
        this.unchanged();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{}", "{\"schoolClassId\":10,\"subjectId\":30}",
            "{\"schoolClassId\":10,\"subjectId\":30,\"hoursPerWeek\":0}",
            "{\"schoolClassId\":10,\"subjectId\":30,\"hoursPerWeek\":-1}",
            "{\"schoolClassId\":10,\"subjectId\":30,\"hoursPerWeek\":3.5}",
            "{\"schoolClassId\":10,\"subjectId\":30,\"hoursPerWeek\":2147483648}",
            "{\"schoolClassId\":true,\"subjectId\":30,\"hoursPerWeek\":3}"
        }
    )
    void rejectsInvalidInputsAtomically(final String input) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json")
                .accept("application/vnd.surok.class-load-v1+json").content(input)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.unchanged();
    }

    @ParameterizedTest
    @CsvSource({"20, 30", "12, 30", "999, 30", "10, 40", "10, 32", "10, 999"})
    void rejectsForeignAndDeletedReferences(final long clazz, final long subject) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json")
                .accept("application/vnd.surok.class-load-v1+json").content(
                    String.format(
                        "{\"schoolClassId\":%d,\"subjectId\":%d,\"hoursPerWeek\":9}", clazz, subject
                    )
                )
        ).andExpect(MockMvcResultMatchers.status().isBadRequest());
        this.unchanged();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "/create", "/50/edit"
        }
    )
    void showsValidationErrorsForForms(final String suffix) throws Exception {
        final String path = String.format("/schools/1/schedules/100/loads%s", suffix);
        this.mvc.perform(
            this.authorized(MockMvcRequestBuilders.post(path), Roles.RoleEnum.DIRECTOR)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("schoolClassId", "10").param("subjectId", "30")
                .param("hoursPerWeek", "bad")
        ).andExpect(MockMvcResultMatchers.status().isFound()).andExpect(
            MockMvcResultMatchers.redirectedUrl(String.format("%s?error=input", path))
        );
        this.mvc.perform(
            MockMvcRequestBuilders.get(path).param("error", "input")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().string(
                Matchers.containsString("положительное целое число часов")
            )
        );
        this.unchanged();
    }

    @Test
    void rejectsDuplicateRevisionAtomically() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.DIRECTOR
            ).contentType("application/vnd.surok.class-load-v1+json")
                .accept("application/vnd.surok.class-load-v1+json")
                .content("{\"schoolClassId\":11,\"subjectId\":31,\"hoursPerWeek\":9}")
        ).andExpect(MockMvcResultMatchers.status().isConflict());
        this.unchanged();
    }

    @Test
    void redirectsDuplicateFormCreation() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads/create"),
                Roles.RoleEnum.DIRECTOR
            ).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("schoolClassId", "10").param("subjectId", "30").param("hoursPerWeek", "4")
        ).andExpect(MockMvcResultMatchers.status().isFound()).andExpect(
            MockMvcResultMatchers.redirectedUrl(
                "/schools/1/schedules/100/loads/create?error=duplicate"
            )
        );
        this.unchanged();
    }

    @ParameterizedTest
    @ValueSource(longs = {52L, 60L, 999L})
    void scopesLoadOperationsAndNeverUpserts(final long identifier) throws Exception {
        final String path = String.format("/schools/1/schedules/100/loads/%d", identifier);
        this.mvc.perform(
            MockMvcRequestBuilders.get(path).session(this.session(Roles.RoleEnum.VIEWER, 1L))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
        this.mvc.perform(
            this.authorized(MockMvcRequestBuilders.put(path), Roles.RoleEnum.DIRECTOR)
                .contentType("application/vnd.surok.class-load-v1+json")
                .content("{\"schoolClassId\":11,\"subjectId\":30,\"hoursPerWeek\":9}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
        this.mvc.perform(
            this.authorized(MockMvcRequestBuilders.delete(path), Roles.RoleEnum.DIRECTOR)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
        this.unchanged();
    }

    @ParameterizedTest
    @ValueSource(longs = {102L, 200L, 999L})
    void scopesSchedules(final long schedule) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get(String.format("/schools/1/schedules/%d/loads", schedule))
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void enforcesSchoolAndRolePermissions() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/2/schedules/200/loads")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/2/schedules/200/loads")
                .session(this.session(Roles.RoleEnum.ADMIN, 1L))
        ).andExpect(MockMvcResultMatchers.status().isOk());
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.delete("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.VIEWER
            )
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/schedules/100/loads/create"),
                Roles.RoleEnum.VIEWER
            ).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("schoolClassId", "11").param("subjectId", "30").param("hoursPerWeek", "4")
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void requiresCsrfAndDeletesAtCanonicalUri() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.delete("/schools/1/schedules/100/loads/50")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.delete("/schools/1/schedules/100/loads/50"),
                Roles.RoleEnum.DIRECTOR
            )
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/50")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "", "/50", "/create"
        }
    )
    void exposesNoApiAliases(final String suffix) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get(
                String.format("/api/schools/1/schedules/100/loads%s", suffix)
            ).session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private void unchanged() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .accept("application/vnd.surok.class-loads.v1+json")
        ).andExpect(MockMvcResultMatchers.jsonPath("$.page.total").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].id").value(50))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].hoursPerWeek").value(5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].schoolClassId").value(10))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].subjectId").value(30));
    }

    private MockHttpServletRequestBuilder authorized(
        final MockHttpServletRequestBuilder request, final Roles.RoleEnum role
    ) throws Exception {
        final MockHttpSession session = this.session(role, 1L);
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/schedules/100/loads/create").session(session)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        return request.session(session).cookie(form.getResponse().getCookies())
            .header(token.getHeaderName(), token.getToken());
    }

    private MockHttpSession session(final Roles.RoleEnum role, final Long school)
        throws Exception {
        final User user = new UrPostgres(
            this.ctx,
            this.ctx.resultQuery(
                """
                INSERT INTO public."user" (username, password, email, corporate, school_id)
                VALUES (?, 'secret', ?, true, ?) RETURNING id
                """,
                UUID.randomUUID().toString(),
                String.format("%s@example.org", UUID.randomUUID()),
                school
            ).fetchSingle("id", Long.class)
        );
        user.roles().grant(role);
        final AuthUser principal = new AuthUser(user);
        final MockHttpSession session = new MockHttpSession();
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            )
        );
        return session;
    }

    @DynamicPropertySource
    private static void properties(final DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.datasource.url", ClsHttpTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", ClsHttpTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", ClsHttpTest.POSTGRES::getPassword);
    }
}
