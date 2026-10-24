/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Teaching load XSLT views and canonical form fields.
 *
 * @since 0.0.1
 */
final class XsLoadsTest {

    @ParameterizedTest
    @ValueSource(strings = {"list", "create", "edit"})
    void printsProtectedFormsAndExternalAssets(final String view) {
        MatcherAssert.assertThat(
            XsLoadsTest.page(view),
            Matchers.allOf(
                Matchers.containsString("name=\"_csrf\" type=\"hidden\" value=\"token\""),
                Matchers.containsString("name=\"schoolClassId\""),
                Matchers.containsString("name=\"subjectId\""),
                Matchers.containsString("name=\"hoursPerWeek\""),
                Matchers.containsString("href=\"/css/school/class-loads.css\""),
                Matchers.containsString("src=\"/js/school/class-loads.js\""),
                Matchers.not(Matchers.containsString("onclick=")),
                Matchers.not(Matchers.containsString("style=")),
                Matchers.not(Matchers.containsString("/api/"))
            )
        );
    }

    @Test
    void createsWithoutCopyingExistingFields() {
        MatcherAssert.assertThat(
            XsLoadsTest.page("create"),
            Matchers.allOf(
                Matchers.containsString("action=\"/schools/3/schedules/9/loads/create\""),
                Matchers.containsString("name=\"hoursPerWeek\""),
                Matchers.containsString("value=\"\""),
                Matchers.not(Matchers.containsString("selected=\"\""))
            )
        );
    }

    @Test
    void keepsSelectedEditingFields() {
        MatcherAssert.assertThat(
            XsLoadsTest.page("edit"),
            Matchers.allOf(
                Matchers.containsString("action=\"/schools/3/schedules/9/loads/7/edit\""),
                Matchers.containsString("value=\"10\" selected=\"\">5А</option>"),
                Matchers.containsString("value=\"30\" selected=\"\">Математика</option>"),
                Matchers.containsString("value=\"5\""),
                Matchers.containsString("Сохранить")
            )
        );
    }

    @Test
    void offersAnAccessibleCreationDialog() {
        MatcherAssert.assertThat(
            XsLoadsTest.page("list"),
            Matchers.allOf(
                Matchers.containsString("<dialog class=\"load-dialog\" id=\"load-create-dialog\""),
                Matchers.containsString("aria-labelledby=\"load-dialog-title\""),
                Matchers.containsString("data-open-load-dialog=\"true\""),
                Matchers.containsString("data-close-load-dialog=\"true\""),
                Matchers.containsString("href=\"/schools/3/schedules/9/loads/create\""),
                Matchers.containsString("href=\"/schools/3/schedules/9/loads/7/edit\""),
                Matchers.containsString("data-uri=\"/schools/3/schedules/9/loads/7\""),
                Matchers.containsString("data-redirect=\"/schools/3/schedules/9/loads\""),
                Matchers.not(Matchers.containsString("/delete"))
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "create", "details", "edit"})
    void keepsScheduleContextInNavigation(final String view) {
        final String page = XsLoadsTest.page(view);
        MatcherAssert.assertThat(
            page.substring(page.indexOf("<aside"), page.indexOf("</aside>")),
            Matchers.allOf(
                Matchers.containsString("href=\"/schools/3/schedules/9/loads\""),
                Matchers.containsString("Учебный год"),
                Matchers.not(Matchers.containsString("/buildings/7")),
                Matchers.not(Matchers.containsString("/classes/7")),
                Matchers.not(Matchers.containsString("/classes/10")),
                Matchers.not(Matchers.containsString("/subjects/30"))
            )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "input, положительное целое число часов",
        "duplicate, уже существует"
    })
    void displaysSafeValidationMessages(final String error, final String expected) {
        MatcherAssert.assertThat(
            new XsClasspath("xslt/class-loads.xsl", "create").transform(
                XsLoadsTest.document().replace(
                    "<error></error>", String.format("<error>%s</error>", error)
                )
            ),
            Matchers.allOf(
                Matchers.containsString("role=\"alert\""),
                Matchers.containsString(expected)
            )
        );
    }

    @Test
    void printsEscapedFragmentWithPagination() {
        MatcherAssert.assertThat(
            new XsClasspath("xslt/class-loads.xsl", "list-fragment").transform(
                XsLoadsTest.document().replace("Математика", "&lt;script&gt;Математика")
            ),
            Matchers.allOf(
                Matchers.containsString("&lt;script&gt;Математика"),
                Matchers.containsString("id=\"loads-results\""),
                Matchers.containsString("id=\"loads-pagination\""),
                Matchers.containsString("data-offset=\"2\""),
                Matchers.containsString("data-limit=\"30\""),
                Matchers.not(Matchers.containsString("<html")),
                Matchers.not(Matchers.containsString("<dialog"))
            )
        );
    }

    @Test
    void showsEmptyState() {
        final String document = XsLoadsTest.document();
        MatcherAssert.assertThat(
            new XsClasspath("xslt/class-loads.xsl", "list-fragment").transform(
                String.format(
                    "%s%s", document.substring(0, document.indexOf("<load>")),
                    document.substring(document.indexOf("</load>") + "</load>".length())
                )
            ),
            Matchers.containsString("Нет данных. Добавьте нагрузку.")
        );
    }

    private static String page(final String view) {
        return new XsClasspath("xslt/class-loads.xsl", view).transform(XsLoadsTest.document());
    }

    private static String document() {
        return """
            <surok>
                <school><id>3</id><name>Школа</name><schedules><schedule>
                    <id>9</id><name>Учебный год</name>
                </schedule></schedules></school>
                <schedule><id>9</id><name>Учебный год</name></schedule>
                <csrf><parameter>_csrf</parameter><token>token</token></csrf><error></error>
                <classes><school-class><id>10</id><name>5А</name></school-class></classes>
                <subjects><subject><id>30</id><name>Математика</name></subject></subjects>
                <loads><load><id>7</id><schoolClassId>10</schoolClassId>
                    <schoolClassName>5А</schoolClassName><subjectId>30</subjectId>
                    <subjectName>Математика</subjectName><hoursPerWeek>5</hoursPerWeek>
                </load><page><offset>1</offset><limit>15</limit><total>30</total>
                    <totalPages>2</totalPages><hasNext>true</hasNext><hasPrev>false</hasPrev>
                </page></loads>
            </surok>
            """;
    }
}
