/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * HTTP representation negotiation.
 *
 * @since 0.0.1
 */
final class AcHttpTest {

    @ParameterizedTest
    @CsvSource(
        delimiter = '|', value = {
            "*/*|text/html",
            "text/html;charset=UTF-8|text/html",
            "application/vnd.surok.room-v1+json|application/vnd.surok.room-v1+json",
            "application/vnd.surok.room-v1+xml|application/vnd.surok.room-v1+xml",
            "text/html;q=0,*/*;q=1|application/vnd.surok.room-v1+json",
            "text/html;q=0.2,application/*;q=0.9|application/vnd.surok.room-v1+json",
            "application/xml;q=0.1,*/*;q=0.8|text/html"
        }
    )
    void selectsPreferredRepresentation(final String header, final String expected) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, header);
        MatcherAssert.assertThat(
            new AcHttp(request, "room-v1").type(),
            Matchers.equalTo(MediaType.valueOf(expected))
        );
    }

    @Test
    void defaultsToHtmlWithoutAccept() {
        MatcherAssert.assertThat(
            new AcHttp(new MockHttpServletRequest(), "room-v1").type(),
            Matchers.equalTo(MediaType.TEXT_HTML)
        );
    }

    @Test
    void honorsExplicitExclusionsBeforeWildcardQuality() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT,
            "text/html;q=0,application/vnd.surok.room-v1+json;q=0,*/*;q=1"
        );
        MatcherAssert.assertThat(
            new AcHttp(request, "room-v1").type(),
            Matchers.equalTo(MediaType.valueOf("application/vnd.surok.room-v1+xml"))
        );
    }

    @Test
    void selectsAnotherSupportedVersion() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(
            HttpHeaders.ACCEPT, "text/html;q=0.1,application/vnd.surok.room-v2+json;q=0.9"
        );
        MatcherAssert.assertThat(
            new AcHttp(request, "room-v1", "room-v2").type(),
            Matchers.equalTo(MediaType.valueOf("application/vnd.surok.room-v2+json"))
        );
    }

    @Test
    void defaultsToFirstExplicitRepresentation() {
        MatcherAssert.assertThat(
            new AcHttp(
                new MockHttpServletRequest(),
                List.of(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
            ).type(),
            Matchers.equalTo(MediaType.APPLICATION_XML)
        );
    }

    @Test
    void rejectsHtmlWhenUnsupported() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, "text/html");
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                ResponseStatusException.class,
                () -> new AcHttp(request, List.of(MediaType.APPLICATION_JSON)).type()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.NOT_ACCEPTABLE)
        );
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "application/json", "application/xml", "application/pdf", "*/*;q=0",
            "invalid", "application/json;q=2", "application/vnd.surok.room-v2+json"
        }
    )
    void rejectsUnavailableRepresentations(final String header) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.ACCEPT, header);
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                ResponseStatusException.class, () -> new AcHttp(request, "room-v1").type()
            ).getStatusCode(),
            Matchers.equalTo(HttpStatus.NOT_ACCEPTABLE)
        );
    }
}
