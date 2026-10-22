/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum;

import com.eshabakhov.surok.user.AuthUser;
import com.eshabakhov.surok.user.CdFake;
import com.eshabakhov.surok.user.RlsFake;
import com.eshabakhov.surok.user.Roles;
import com.eshabakhov.surok.user.UrFake;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Shared Accept negotiation in federal curriculum and requirement HTTP resources.
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
final class CrsHttpTest {

    /** Postgres container. */
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("surok")
            .withUsername("test")
            .withPassword("test");

    /** Database context. */
    private final DSLContext ctx;

    /** HTTP client with security filters. */
    private final MockMvc mvc;

    @Autowired
    CrsHttpTest(final DSLContext ctx, final MockMvc mvc) {
        this.ctx = ctx;
        this.mvc = mvc;
    }

    @BeforeEach
    void insertCurriculumAndRequirement() {
        this.ctx.resultQuery(
            """
                INSERT INTO public.federal_curriculum (id, title, education_level, study_week_type,
                version, academic_year, description, is_deleted)
                VALUES (DEFAULT, 'Basic plan', 'BASIC', 'FIVE_DAYS', '1', '2026/2027',
                    'Description', false)
                """
        ).execute();
        this.ctx.resultQuery(
            """
                INSERT INTO public.federal_curriculum_requirement (id, federal_curriculum_id, grade,
                subject_name, weekly_hours, part_type, is_deleted)
                VALUES (7, 1, 5, 'Mathematics', 5, 'MANDATORY', false)
                """
        ).execute();
    }

    @AfterEach
    void truncateCurriculums() {
        this.ctx.resultQuery(
            """
                TRUNCATE TABLE public.federal_curriculum RESTART IDENTITY CASCADE
                """
        ).execute();
    }

    @ParameterizedTest
    @CsvSource({
        "/1, federal-curriculum-v1, json",
        "/1, federal-curriculum-v2, json",
        "/1/edit, federal-curriculum-v1, xml",
        "/1/edit, federal-curriculum-v2, xml",
        "'', federal-curriculums.v1, json",
        "/fragment, federal-curriculums.v1, xml",
        "/1/requirements, federal-curriculum-requirements.v1, json",
        "/1/requirements/fragment, federal-curriculum-requirements.v1, xml",
        "/1/requirements/7, federal-curriculum-requirement-v1, json",
        "/1/requirements/7, federal-curriculum-requirement-v1, xml"
    })
    void negotiatesVersionedRead(
        final String path, final String resource, final String format
    ) throws Exception {
        final String expected = String.format("application/vnd.surok.%s+%s", resource, format);
        this.mvc.perform(
            MockMvcRequestBuilders.get(String.format("/federal/curriculums%s", path))
                .session(this.session())
                .header("Accept", String.format("text/html;q=0.1,%s;q=0.9", expected))
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(expected)
        ).andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.isEmptyString())));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"*/*", "text/html;q=0.2,application/json;q=0.9"})
    void defaultsHtmlPagesToHtml(final String header) throws Exception {
        for (final String path : List.of(
            "", "/create", "/fragment", "/1", "/1/edit",
            "/1/requirements", "/1/requirements/fragment"
        )) {
            final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(
                String.format("/federal/curriculums%s", path)
            ).session(this.session());
            if (header != null) {
                request.header("Accept", header);
            }
            this.mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith("text/html"));
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"*/*", "text/html;q=0,*/*;q=1"})
    void defaultsRequirementToJson(final String header) throws Exception {
        final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(
            "/federal/curriculums/1/requirements/7"
        ).session(this.session());
        if (header != null) {
            request.header("Accept", header);
        }
        this.mvc.perform(request).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(
                "application/vnd.surok.federal-curriculum-requirement-v1+json"
            )
        ).andExpect(MockMvcResultMatchers.jsonPath("$.id").value(7));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "application/xml", "*/*;q=0", "invalid"})
    void rejectsUnsupportedRead(final String header) throws Exception {
        for (final String path : List.of("", "/1", "/1/requirements", "/1/requirements/7")) {
            this.mvc.perform(
                MockMvcRequestBuilders.get(String.format("/federal/curriculums%s", path))
                    .session(this.session()).header("Accept", header)
            ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        }
    }

    @ParameterizedTest
    @CsvSource({
        "/create, federal-curriculum-v1, json",
        "/create, federal-curriculum-v2, xml",
        "/1/edit, federal-curriculum-v2, json",
        "/1/edit, federal-curriculum-v1, xml",
        "/1/requirements/create, federal-curriculum-requirement-v1, json",
        "/1/requirements/create, federal-curriculum-requirement-v1, xml",
        "/1/requirements/7/edit, federal-curriculum-requirement-v1, json",
        "/1/requirements/7/edit, federal-curriculum-requirement-v1, xml"
    })
    void negotiatesBeforeMutation(
        final String path, final String resource, final String format
    ) throws Exception {
        final String expected = String.format("application/vnd.surok.%s+%s", resource, format);
        final int status;
        if (path.endsWith("/create")) {
            status = 201;
        } else {
            status = 200;
        }
        final ResultActions result = this.mvc.perform(
            this.authorized(path).header(
                "Accept", String.format("text/html;q=0.1,%s;q=0.9", expected)
            )
        ).andExpect(MockMvcResultMatchers.status().is(status))
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(expected))
            .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("After")));
        if (status == 201) {
            result.andExpect(
                MockMvcResultMatchers.header().string(
                    "Location",
                    Matchers.matchesPattern("/federal/curriculums/[0-9]+(/requirements/[0-9]+)?")
                )
            );
        } else {
            result.andExpect(MockMvcResultMatchers.header().doesNotExist("Location"));
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {"application/json", "application/xml", "*/*;q=0", "invalid", "application/pdf"}
    )
    void rejectsUnsupportedAcceptBeforeMutation(final String header) throws Exception {
        for (final String path : List.of(
            "/create", "/1/edit", "/1/requirements/create", "/1/requirements/7/edit"
        )) {
            this.mvc.perform(this.authorized(path).header("Accept", header))
                .andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        }
        MatcherAssert.assertThat(
            this.ctx.resultQuery(
                """
                    SELECT title AS value FROM public.federal_curriculum
                    UNION ALL
                    SELECT subject_name AS value FROM public.federal_curriculum_requirement
                    """
            ).fetch().getValues("value", String.class),
            Matchers.containsInAnyOrder("Basic plan", "Mathematics")
        );
    }

    @ParameterizedTest
    @CsvSource({
        "/create, /federal/curriculums/[0-9]+",
        "/1/edit, /federal/curriculums/1",
        "/1/requirements/create, /federal/curriculums/1/requirements",
        "/1/requirements/7/edit, /federal/curriculums/1/requirements"
    })
    void redirectsHtmlMutation(final String path, final String location) throws Exception {
        this.mvc.perform(this.authorized(path).accept(MediaType.TEXT_HTML))
            .andExpect(MockMvcResultMatchers.status().isFound()).andExpect(
                MockMvcResultMatchers.header().string("Location", Matchers.matchesPattern(location))
            ).andExpect(MockMvcResultMatchers.content().string(""));
    }

    @ParameterizedTest
    @CsvSource({
        "/1, federal_curriculum, 1",
        "/1/requirements/7, federal_curriculum_requirement, 7"
    })
    void removesWithoutResponseBody(
        final String path, final String table, final long identifier
    ) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.delete(String.format("/federal/curriculums%s", path))
            )
        ).andExpect(MockMvcResultMatchers.status().isNoContent())
            .andExpect(MockMvcResultMatchers.content().string(""));
        MatcherAssert.assertThat(
            this.ctx.resultQuery(
                String.format("SELECT is_deleted FROM public.%s WHERE id = ?", table), identifier
            ).fetchOneInto(Boolean.class),
            Matchers.is(true)
        );
    }

    private MockHttpServletRequestBuilder authorized(final String path) throws Exception {
        return this.authorized(
            MockMvcRequestBuilders.post(String.format("/federal/curriculums%s", path))
        ).contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("title", "After").param("level", "BASIC").param("week", "FIVE_DAYS")
            .param("version", "2").param("year", "2026/2027").param("description", "Description")
            .param("grade", "6").param("subjectName", "After").param("weeklyHours", "2")
            .param("partType", "OPTIONAL");
    }

    private MockHttpServletRequestBuilder authorized(final MockHttpServletRequestBuilder request)
        throws Exception {
        final MockHttpSession session = this.session();
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/federal/curriculums/create").session(session)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        return request.session(session).cookie(form.getResponse().getCookies())
            .header(token.getHeaderName(), token.getToken());
    }

    private MockHttpSession session() throws Exception {
        final Roles roles = new RlsFake();
        roles.grant(Roles.RoleEnum.ADMIN);
        final AuthUser principal = new AuthUser(
            new UrFake(1L, new CdFake("admin", "secret"), roles)
        );
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
        registry.add("spring.datasource.url", CrsHttpTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", CrsHttpTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", CrsHttpTest.POSTGRES::getPassword);
    }
}
