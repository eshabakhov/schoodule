/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * HTML serialization preserves values and contents while using explicit attributes.
 *
 * @since 0.0.1
 */
final class HtmlOutputTest {

    @Test
    void formatsTheSerializerGeneratedHead() {
        MatcherAssert.assertThat(
            new HtmlOutput().apply(
                String.format(
                    "<!DOCTYPE html SYSTEM \"about:legacy-compat\"><html><head>%s</head></html>",
                    "<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
                )
            ),
            Matchers.allOf(
                Matchers.startsWith("<!DOCTYPE html>"),
                Matchers.containsString(
                    "<meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">"
                )
            )
        );
    }

    @Test
    void preservesBooleanPresenceAndEmptyValues() {
        MatcherAssert.assertThat(
            new HtmlOutput().apply("<option selected value=\"\">Class</option>"),
            Matchers.is("<option value=\"\" selected=\"\">Class</option>")
        );
    }

    @Test
    void preservesAttributeValuesContainingMarkup() {
        MatcherAssert.assertThat(
            new HtmlOutput().apply(
                "<a title=\"<option selected> $1 &quot;name&quot;\" href=\"/schools?a=1&amp;b=2\">"
            ),
            Matchers.is(
                "<a href=\"/schools?a=1&amp;b=2\" title=\"<option selected> $1 &quot;name&quot;\">"
            )
        );
    }

    @Test
    void leavesCommentsIntact() {
        MatcherAssert.assertThat(
            new HtmlOutput().apply("<!-- <option selected> -->"),
            Matchers.is("<!-- <option selected> -->")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"script", "style", "textarea", "title"})
    void preservesRawContents(final String name) {
        MatcherAssert.assertThat(
            new HtmlOutput().apply(
                String.format("<%1$s><option selected>text</%1$s><input disabled>", name)
            ),
            Matchers.is(
                String.format("<%1$s><option selected>text</%1$s><input disabled=\"\">", name)
            )
        );
    }

    @Test
    void preservesForeignElementAndAttributeNames() {
        MatcherAssert.assertThat(
            new HtmlOutput().apply(
                "<svg viewBox=\"0 0 24 24\" xmlns=\"http://www.w3.org/2000/svg\">"
            ),
            Matchers.is("<svg viewBox=\"0 0 24 24\" xmlns=\"http://www.w3.org/2000/svg\">")
        );
    }

    @Test
    void preservesDuplicateAttributesForValidation() {
        MatcherAssert.assertThat(
            new HtmlOutput().apply("<div id=\"one\" id=\"two\">"),
            Matchers.is("<div id=\"one\" id=\"two\">")
        );
    }
}
