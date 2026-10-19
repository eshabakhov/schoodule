/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filter;
import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.enums.EducationLevelType;
import com.eshabakhov.schoodule.enums.StudyWeekType;
import com.eshabakhov.schoodule.filter.FiltersRequest;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.sort.SortsRequest;
import com.eshabakhov.schoodule.tables.FederalCurriculum;
import com.eshabakhov.schoodule.tables.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.user.CdFake;
import com.eshabakhov.schoodule.user.RlsFake;
import com.eshabakhov.schoodule.user.Roles;
import com.eshabakhov.schoodule.user.UrFake;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for {@link RqsHttp}.
 *
 * @since 0.0.1
 * @checkstyle ProhibitFieldsInTestClassesCheck (1000 lines)
 */
@Testcontainers
@SpringBootTest
@SuppressWarnings({
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.UnusedPrivateMethod",
    "UnusedMethod"
})
final class RqsHttpTest {

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
    RqsHttpTest(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @BeforeEach
    void insertCurriculumAndRequirement() {
        this.ctx.insertInto(FederalCurriculum.FEDERAL_CURRICULUM)
            .set(FederalCurriculum.FEDERAL_CURRICULUM.ID, 1L)
            .set(FederalCurriculum.FEDERAL_CURRICULUM.TITLE, "Basic plan").set(
                FederalCurriculum.FEDERAL_CURRICULUM.EDUCATION_LEVEL,
                EducationLevelType.BASIC
            ).set(
                FederalCurriculum.FEDERAL_CURRICULUM.STUDY_WEEK_TYPE,
                StudyWeekType.FIVE_DAYS
            )
            .set(FederalCurriculum.FEDERAL_CURRICULUM.VERSION, "1").set(
                FederalCurriculum.FEDERAL_CURRICULUM.ACADEMIC_YEAR,
                "2026/2027"
            ).set(
                FederalCurriculum.FEDERAL_CURRICULUM.DESCRIPTION,
                "Basic federal plan"
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
            5
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .SUBJECT_NAME,
            "Mathematics"
        ).set(
            FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                .WEEKLY_HOURS,
            5
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
    void cleanContextAndCurriculums() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
        this.ctx.truncate(FederalCurriculum.FEDERAL_CURRICULUM)
            .restartIdentity().cascade().execute();
    }

    @Test
    void returnsRequirementsHtmlPage() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "*/*");
        request.setRequestURI("/federal/curriculums/1/requirements");
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final Roles roles = new RlsFake();
        roles.grant(Roles.RoleEnum.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new UrFake(1L, new CdFake("admin", "secret"), roles),
                null,
                List.of()
            )
        );
        MatcherAssert.assertThat(
            "HTML request must return the complete requirements page",
            (String) new RqsHttp(this.ctx).selection(
                1L,
                new FiltersRequest(Set.<Filter>of()),
                new PageRequest(15, 1),
                new SortsRequest(List.<Sort>of())
            ),
            Matchers.allOf(
                Matchers.containsString("<html lang=\"ru\">"),
                Matchers.containsString("Basic plan"),
                Matchers.containsString("Mathematics")
            )
        );
    }

    @Test
    void returnsRequirementsHtmlFragment() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "text/html");
        request.setRequestURI(
            "/federal/curriculums/1/requirements/fragment"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final Roles roles = new RlsFake();
        roles.grant(Roles.RoleEnum.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new UrFake(1L, new CdFake("admin", "secret"), roles),
                null,
                List.of()
            )
        );
        MatcherAssert.assertThat(
            "Fragment request must return only requirement results",
            (String) new RqsHttp(this.ctx).selection(
                1L,
                new FiltersRequest(Set.<Filter>of()),
                new PageRequest(15, 1),
                new SortsRequest(List.<Sort>of())
            ),
            Matchers.allOf(
                Matchers.not(Matchers.containsString("<html")),
                Matchers.containsString("Mathematics")
            )
        );
    }

    @Test
    void returnsRequirementsHtmlForNullUri() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "text/html");
        request.setRequestURI(null);
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final Roles roles = new RlsFake();
        roles.grant(Roles.RoleEnum.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new UrFake(1L, new CdFake("admin", "secret"), roles),
                null,
                List.of()
            )
        );
        MatcherAssert.assertThat(
            "Null request URI must select the complete requirements page",
            (String) new RqsHttp(this.ctx).selection(
                1L,
                new FiltersRequest(Set.<Filter>of()),
                new PageRequest(15, 1),
                new SortsRequest(List.<Sort>of())
            ),
            Matchers.containsString("<html lang=\"ru\">")
        );
    }

    @Test
    void returnsRequirementsJson() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirements.v1+json"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final ObjectNode response = (ObjectNode) new RqsHttp(this.ctx).selection(
            1L,
            new FiltersRequest(Set.<Filter>of()),
            new PageRequest(15, 1),
            new SortsRequest(List.<Sort>of())
        );
        MatcherAssert.assertThat(
            "JSON requirements must contain the selected requirement",
            response.path("items").get(0).path("subjectName").asText(),
            Matchers.is("Mathematics")
        );
        MatcherAssert.assertThat(
            "JSON requirements must contain pagination",
            response.path("page").path("total").asInt(),
            Matchers.is(1)
        );
    }

    @Test
    void returnsRequirementsXml() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirements.v1+xml"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        MatcherAssert.assertThat(
            "XML requirements must use the collection root",
            (String) new RqsHttp(this.ctx).selection(
                1L,
                new FiltersRequest(Set.<Filter>of()),
                new PageRequest(15, 1),
                new SortsRequest(List.<Sort>of())
            ),
            Matchers.allOf(
                Matchers.startsWith("<requirements>"),
                Matchers.containsString("<requirement>"),
                Matchers.endsWith("</requirements>")
            )
        );
    }

    @Test
    void rejectsRequirementsWithoutAccept() {
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
            )
        );
        MatcherAssert.assertThat(
            "Missing Accept must result in 406",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new RqsHttp(this.ctx).selection(
                    1L,
                    new FiltersRequest(Set.<Filter>of()),
                    new PageRequest(15, 1),
                    new SortsRequest(List.<Sort>of())
                )
            ).getStatusCode(),
            Matchers.is(HttpStatus.NOT_ACCEPTABLE)
        );
    }

    @Test
    void rejectsUnsupportedRequirementsAccept() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "application/json");
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        MatcherAssert.assertThat(
            "Unsupported Accept must result in 406",
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new RqsHttp(this.ctx).selection(
                    1L,
                    new FiltersRequest(Set.<Filter>of()),
                    new PageRequest(15, 1),
                    new SortsRequest(List.<Sort>of())
                )
            ).getStatusCode(),
            Matchers.is(HttpStatus.NOT_ACCEPTABLE)
        );
    }

    @Test
    void returnsRequirementJson() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+json"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        MatcherAssert.assertThat(
            "JSON requirement must contain its identity",
            ((ObjectNode) new RqsHttp(this.ctx).requirement(1L, 7L))
                .path("id").longValue(),
            Matchers.is(7L)
        );
    }

    @Test
    void returnsRequirementXml() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        MatcherAssert.assertThat(
            "XML requirement must use the entity root",
            (String) new RqsHttp(this.ctx).requirement(1L, 7L),
            Matchers.allOf(
                Matchers.startsWith("<requirement>"),
                Matchers.containsString("<id>7</id>"),
                Matchers.endsWith("</requirement>")
            )
        );
    }

    @Test
    void rejectsRequirementWithoutAccept() {
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
            )
        );
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RqsHttp(this.ctx).requirement(1L, 7L)
        );
    }

    @Test
    void rejectsUnsupportedRequirementAccept() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "application/xml");
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RqsHttp(this.ctx).requirement(1L, 7L)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/html", "*/*"})
    void createsRequirementForHtml(final String accept) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, accept);
        final MockHttpServletResponse response =
            new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "6");
        parameters.add("subjectName", "History");
        parameters.add("weeklyHours", "2");
        parameters.add("partType", "OPTIONAL");
        MatcherAssert.assertThat(
            "HTML creation must not return a response body",
            new RqsHttp(this.ctx).create(1L, parameters),
            Matchers.nullValue()
        );
        MatcherAssert.assertThat(
            "HTML creation must redirect to requirements",
            response.getRedirectedUrl(),
            Matchers.is("/federal/curriculums/1/requirements")
        );
    }

    @Test
    void createsRequirementAsJson() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+json"
        );
        final MockHttpServletResponse response =
            new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "6");
        parameters.add("subjectName", "History");
        parameters.add("weeklyHours", "2");
        parameters.add("partType", "OPTIONAL");
        new RqsHttp(this.ctx).create(
            1L,
            parameters
        );
        MatcherAssert.assertThat(
            "JSON creation must return status 201",
            response.getStatus(),
            Matchers.is(HttpStatus.CREATED.value())
        );
        MatcherAssert.assertThat(
            "JSON creation must expose the created location",
            response.getHeader(HttpHeaders.LOCATION),
            Matchers.matchesPattern(
                "/federal/curriculums/1/requirements/[0-9]+"
            )
        );
    }

    @Test
    void createsRequirementAsXml() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
        );
        final MockHttpServletResponse response =
            new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "6");
        parameters.add("subjectName", "History");
        parameters.add("weeklyHours", "2");
        parameters.add("partType", "OPTIONAL");
        MatcherAssert.assertThat(
            "XML creation must return the created requirement",
            (String) new RqsHttp(this.ctx).create(1L, parameters),
            Matchers.containsString("<subjectName>History</subjectName>")
        );
        MatcherAssert.assertThat(
            "XML creation must return status 201",
            response.getStatus(),
            Matchers.is(HttpStatus.CREATED.value())
        );
    }

    @Test
    void rejectsCreationWithoutAccept() {
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
            )
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "6");
        parameters.add("subjectName", "History");
        parameters.add("weeklyHours", "2");
        parameters.add("partType", "OPTIONAL");
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RqsHttp(this.ctx).create(1L, parameters)
        );
    }

    @Test
    void rejectsUnsupportedCreationAccept() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "application/json");
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "6");
        parameters.add("subjectName", "History");
        parameters.add("weeklyHours", "2");
        parameters.add("partType", "OPTIONAL");
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RqsHttp(this.ctx).create(1L, parameters)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/html", "*/*"})
    void revisesRequirementForHtml(final String accept) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, accept);
        final MockHttpServletResponse response =
            new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "7");
        parameters.add("subjectName", "Algebra");
        parameters.add("weeklyHours", "4");
        parameters.add("partType", "MANDATORY");
        MatcherAssert.assertThat(
            "HTML revision must not return a response body",
            new RqsHttp(this.ctx).revise(1L, 7L, parameters),
            Matchers.nullValue()
        );
        MatcherAssert.assertThat(
            "HTML revision must redirect to requirements",
            response.getRedirectedUrl(),
            Matchers.is("/federal/curriculums/1/requirements")
        );
    }

    @Test
    void revisesRequirementAsJson() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+json"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "7");
        parameters.add("subjectName", "Algebra");
        parameters.add("weeklyHours", "4");
        parameters.add("partType", "MANDATORY");
        MatcherAssert.assertThat(
            "JSON revision must return revised fields",
            ((ObjectNode) new RqsHttp(this.ctx).revise(1L, 7L, parameters))
                .path("subjectName").asText(),
            Matchers.is("Algebra")
        );
    }

    @Test
    void revisesRequirementAsXml() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
        );
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "7");
        parameters.add("subjectName", "Algebra");
        parameters.add("weeklyHours", "4");
        parameters.add("partType", "MANDATORY");
        MatcherAssert.assertThat(
            "XML revision must return revised fields",
            (String) new RqsHttp(this.ctx).revise(1L, 7L, parameters),
            Matchers.containsString("<subjectName>Algebra</subjectName>")
        );
    }

    @Test
    void rejectsRevisionWithoutAccept() {
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
            )
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "7");
        parameters.add("subjectName", "Algebra");
        parameters.add("weeklyHours", "4");
        parameters.add("partType", "MANDATORY");
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RqsHttp(this.ctx).revise(1L, 7L, parameters)
        );
    }

    @Test
    void rejectsUnsupportedRevisionAccept() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "application/xml");
        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(
                request,
                new MockHttpServletResponse()
            )
        );
        final MultiValueMap<String, String> parameters =
            new LinkedMultiValueMap<>();
        parameters.add("grade", "7");
        parameters.add("subjectName", "Algebra");
        parameters.add("weeklyHours", "4");
        parameters.add("partType", "MANDATORY");
        Assertions.assertThrows(
            ResponseStatusException.class,
            () -> new RqsHttp(this.ctx).revise(1L, 7L, parameters)
        );
    }

    @Test
    void removesRequirement() throws Exception {
        new RqsHttp(this.ctx).remove(1L, 7L);
        MatcherAssert.assertThat(
            "HTTP removal must mark the requirement as deleted",
            this.ctx.select(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
                    .IS_DELETED
            ).from(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT
            ).where(
                FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT.ID
                    .eq(7L)
            ).fetchOneInto(Boolean.class),
            Matchers.is(true)
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
            RqsHttpTest.POSTGRES::getJdbcUrl
        );
        registry.add(
            "spring.datasource.username",
            RqsHttpTest.POSTGRES::getUsername
        );
        registry.add(
            "spring.datasource.password",
            RqsHttpTest.POSTGRES::getPassword
        );
        registry.add("spring.liquibase.enabled", () -> true);
    }
}
