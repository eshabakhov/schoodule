/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.User;
import com.eshabakhov.surok.user.AuthUser;
import com.eshabakhov.surok.user.CdFake;
import com.eshabakhov.surok.user.RiFake;
import com.eshabakhov.surok.user.RlsFake;
import com.eshabakhov.surok.user.Roles;
import java.util.List;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
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
 * Existing building HTTP contracts verified with MockMvc assertions.
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
final class BdsHttpTest {

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
    BdsHttpTest(final DSLContext ctx, final MockMvc mvc) {
        this.ctx = ctx;
        this.mvc = mvc;
    }

    @BeforeEach
    void insertSchoolsAndBuildings() {
        this.ctx.resultQuery(
            """
                INSERT INTO public.school (id, name)
                VALUES (1, 'First school'), (2, 'Second school')
                """
        ).execute();
        this.ctx.resultQuery(
            """
                INSERT INTO public.building (id, school_id, name, is_deleted)
                VALUES (10, 1, 'North', false), (11, 1, 'South', false),
                    (12, 1, 'North removed', true), (20, 2, 'North foreign', false)
                """
        ).execute();
        this.ctx.resultQuery(
            """
                INSERT INTO public.cabinet (id, building_id, name)
                VALUES (30, 10, 'Room')
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
            MockMvcRequestBuilders.get("/schools/1/buildings").accept(MediaType.TEXT_HTML)
        ).andExpect(MockMvcResultMatchers.status().isFound())
            .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost/users/login"));
    }

    @Test
    void printsJsonPage() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/")
                .accept(MediaType.APPLICATION_JSON).session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .param("name_ct", "north").param("limit", "1")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.items[0].name").value("North"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.total").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.limit").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.offset").value(1));
    }

    @Test
    void printsJsonBuilding() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .accept(MediaType.APPLICATION_JSON).session(this.session(Roles.RoleEnum.VIEWER, 1L))
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().json("{\"id\":10,\"name\":\"North\"}"));
    }

    @Test
    void deniesAnotherSchool() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/2/buildings/20")
                .accept(MediaType.APPLICATION_JSON)
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void deniesJsonCreationForViewer() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/buildings"), Roles.RoleEnum.VIEWER
            ).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"East\"}")
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void createsBuildingFromJson() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/buildings"),
                Roles.RoleEnum.DIRECTOR
            ).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"East\"}")
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(
            MockMvcResultMatchers.header().string(
                "Location", Matchers.startsWith("/schools/1/buildings/")
            )
        ).andExpect(MockMvcResultMatchers.jsonPath("$.name").value("East"));
    }

    @ParameterizedTest
    @ValueSource(longs = {12L, 20L, 999L})
    void rejectsRevisionOfUnavailableBuilding(final long identifier) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/buildings/{id}", identifier),
                Roles.RoleEnum.DIRECTOR
            ).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"West\"}")
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON).param("name_ct", "West")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.total").value(0));
    }

    @Test
    void revisesBuildingFromJson() throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/buildings/10"), Roles.RoleEnum.DIRECTOR
            ).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Renamed\"}")
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().json("{\"id\":10,\"name\":\"Renamed\"}")
        );
    }

    @Test
    void rendersBuildingAndCabinetPages() throws Exception {
        for (final String path : List.of(
            "", "/fragment", "/create", "/10", "/10/edit",
            "/10/cabinets", "/10/cabinets/create", "/10/cabinets/30",
            "/10/cabinets/30/edit"
        )) {
            this.mvc.perform(
                MockMvcRequestBuilders.get(String.format("/schools/1/buildings%s", path))
                    .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
            ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
                MockMvcResultMatchers.content().string(Matchers.not(Matchers.isEmptyString()))
            );
        }
    }

    @Test
    void createsThroughProtectedForm() throws Exception {
        final MockHttpSession session = this.session(Roles.RoleEnum.DIRECTOR, 1L);
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/create").session(session)
        ).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        this.mvc.perform(
            MockMvcRequestBuilders.post("/schools/1/buildings/create")
                .session(session).cookie(form.getResponse().getCookies())
                .param(token.getParameterName(), token.getToken())
                .param("name", " Web building ")
        ).andExpect(MockMvcResultMatchers.status().isFound())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/schools/1/buildings"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/json", "application/vnd.surok.building-v1+json"})
    void negotiatesJsonOnBrowserRoute(final String accept) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L)).header("Accept", accept)
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(accept))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("North"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/xml", "application/vnd.surok.building-v1+xml"})
    void negotiatesXmlOnBrowserRoute(final String accept) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L)).header("Accept", accept)
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(accept))
            .andExpect(MockMvcResultMatchers.xpath("/building/name").string("North"));
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = '|', value = {
            "text/html;q=0.2,application/json;q=0.9|application/json",
            "text/html;q=0,*/*;q=1|application/json",
            "application/xml;q=0.1,*/*;q=0.8|text/html",
            "application/vnd.surok.building-v1+xml|application/vnd.surok.building-v1+xml",
            "text/html;charset=UTF-8|text/html",
            "*/*|text/html"
        }
    )
    void honorsAcceptPriorities(final String accept, final String expected) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L)).header("Accept", accept)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(expected)
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "application/pdf", "*/*;q=0", "invalid", "application/json;q=2",
            "application/com.eshabakhov.surok.school.building.simplebuilding+json",
            "application/vnd.surok.buildings.v1+json"
        }
    )
    void rejectsUnavailableRepresentations(final String accept) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L)).header("Accept", accept)
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
    }

    @Test
    void printsVersionedCollection() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings")
                .session(this.session(Roles.RoleEnum.VIEWER, 1L))
                .header("Accept", "application/vnd.surok.buildings.v1+json")
                .param("filter", "name:North").param("limit", "1")
                .param("sort", "name:DESC")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.items.length()").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.limit").value(1));
    }

    @Test
    void rendersXsltWithNavigationAndToken() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/create")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .header("Accept", "text/html,application/xhtml+xml;q=0.9,*/*;q=0.8")
        ).andExpect(MockMvcResultMatchers.status().isOk()).andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith("text/html")
        ).andExpect(
            MockMvcResultMatchers.content().string(Matchers.containsString("name=\"_csrf\""))
        ).andExpect(
            MockMvcResultMatchers.content().string(
                Matchers.containsString("href=\"/schools/1/subjects\"")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "*/*;q=0", "invalid", "application/json;q=2"})
    void rejectsUnsupportedRepresentationBeforeCreation(final String accept) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/buildings"),
                Roles.RoleEnum.DIRECTOR
            ).header("Accept", accept).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Rejected\"}")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON).param("name_ct", "Rejected")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.total").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "*/*;q=0", "invalid", "application/json;q=2"})
    void rejectsUnsupportedRepresentationBeforeFormCreation(final String accept) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/buildings/create"),
                Roles.RoleEnum.DIRECTOR
            ).header("Accept", accept).param("name", "Rejected")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON).param("name_ct", "Rejected")
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.page.total").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "*/*;q=0", "invalid", "application/json;q=2"})
    void rejectsUnsupportedRepresentationBeforeRevision(final String accept) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.put("/schools/1/buildings/10"),
                Roles.RoleEnum.DIRECTOR
            ).header("Accept", accept).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Rejected\"}")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("North"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "*/*;q=0", "invalid", "application/json;q=2"})
    void rejectsUnsupportedRepresentationBeforeFormRevision(final String accept) throws Exception {
        this.mvc.perform(
            this.authorized(
                MockMvcRequestBuilders.post("/schools/1/buildings/10/edit"),
                Roles.RoleEnum.DIRECTOR
            ).header("Accept", accept).param("name", "Rejected")
        ).andExpect(MockMvcResultMatchers.status().isNotAcceptable());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("North"));
    }

    @Test
    void deniesFormMutationForViewer() throws Exception {
        final MockHttpSession session = this.session(Roles.RoleEnum.VIEWER, 1L);
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/create").session(session)
        ).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        this.mvc.perform(
            MockMvcRequestBuilders.post("/schools/1/buildings/create")
                .session(session).cookie(form.getResponse().getCookies())
                .param(token.getParameterName(), token.getToken()).param("name", "Forbidden")
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void requiresCsrfOnBrowserMutation() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.post("/schools/1/buildings/create")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L)).param("name", "Forbidden")
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void requiresCsrfOnJsonMutation() throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.post("/schools/1/buildings")
                .session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Forbidden\"}")
        ).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    void createsXmlThroughCanonicalForm() throws Exception {
        final MockHttpSession session = this.session(Roles.RoleEnum.DIRECTOR, 1L);
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/create").session(session)
        ).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        this.mvc.perform(
            MockMvcRequestBuilders.post("/schools/1/buildings/create")
                .session(session).cookie(form.getResponse().getCookies())
                .param(token.getParameterName(), token.getToken()).param("name", " Актовый зал ")
                .header("Accept", "application/vnd.surok.building-v1+xml")
        ).andExpect(MockMvcResultMatchers.status().isCreated()).andExpect(
            MockMvcResultMatchers.header().string(
                "Location", Matchers.startsWith("/schools/1/buildings/")
            )
        ).andExpect(MockMvcResultMatchers.xpath("/building/name").string("Актовый зал"));
    }

    @Test
    void removesThroughCanonicalRouteWithCsrfHeader() throws Exception {
        final MockHttpSession session = this.session(Roles.RoleEnum.DIRECTOR, 1L);
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10/edit").session(session)
        ).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        this.mvc.perform(
            MockMvcRequestBuilders.delete("/schools/1/buildings/10")
                .session(session).cookie(form.getResponse().getCookies())
                .header(token.getHeaderName(), token.getToken())
        ).andExpect(MockMvcResultMatchers.status().isNoContent());
        this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/10").session(session)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/", "/10", "/create", "/fragment", "/10/edit"})
    void doesNotExposeApiAliases(final String suffix) throws Exception {
        this.mvc.perform(
            MockMvcRequestBuilders.get(
                String.format("/api/schools/1/buildings%s", suffix)
            ).session(this.session(Roles.RoleEnum.DIRECTOR, 1L))
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private MockHttpServletRequestBuilder authorized(
        final MockHttpServletRequestBuilder request, final Roles.RoleEnum role
    ) throws Exception {
        final MockHttpSession session = this.session(role, 1L);
        final MvcResult form = this.mvc.perform(
            MockMvcRequestBuilders.get("/schools/1/buildings/create").session(session)
        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        final CsrfToken token = (CsrfToken) form.getRequest().getAttribute(
            CsrfToken.class.getName()
        );
        return request.session(session).cookie(form.getResponse().getCookies())
            .header(token.getHeaderName(), token.getToken());
    }

    private MockHttpSession session(final Roles.RoleEnum role, final Long school)
        throws Exception {
        final User user = Mockito.mock(User.class);
        final Roles roles = new RlsFake();
        roles.grant(role);
        Mockito.when(user.roles()).thenReturn(roles);
        Mockito.when(user.info()).thenReturn(new RiFake(school, false, true));
        Mockito.when(user.credentials()).thenReturn(new CdFake("director", "password"));
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
        registry.add("spring.datasource.url", BdsHttpTest.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", BdsHttpTest.POSTGRES::getUsername);
        registry.add("spring.datasource.password", BdsHttpTest.POSTGRES::getPassword);
    }
}
