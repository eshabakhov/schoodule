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
 * School class XSLT views preserve form values and safe navigation.
 *
 * @since 0.0.1
 */
final class XsSchoolClassesTest {

    @ParameterizedTest
    @ValueSource(strings = {"create", "edit"})
    void printsProtectedForms(final String view) {
        MatcherAssert.assertThat(
            XsSchoolClassesTest.page(view),
            Matchers.allOf(
                Matchers.containsString("name=\"_csrf\" value=\"token\""),
                Matchers.containsString("id=\"grade-name\" name=\"grade\" type=\"number\""),
                Matchers.containsString("id=\"litera-name\" name=\"litera\""),
                Matchers.containsString("Укажите параллель от 1 до 11 и непустую литеру"),
                Matchers.containsString("href=\"/css/school/school-classes.css\""),
                Matchers.containsString("src=\"/js/school/school-classes.js\""),
                Matchers.not(Matchers.containsString("style=\""))
            )
        );
    }

    @Test
    void leavesCreationFieldsEmpty() {
        final String page = XsSchoolClassesTest.page("create");
        MatcherAssert.assertThat(
            page.substring(page.indexOf("<main")),
            Matchers.allOf(
                Matchers.containsString("action=\"/schools/3/classes/create\""),
                Matchers.containsString("value=\"\""),
                Matchers.containsString("placeholder=\"Например: 10\""),
                Matchers.not(Matchers.containsString("value=\"5\"")),
                Matchers.not(Matchers.containsString("value=\"А\""))
            )
        );
    }

    @Test
    void printsBothEditingValues() {
        MatcherAssert.assertThat(
            XsSchoolClassesTest.page("edit"),
            Matchers.allOf(
                Matchers.containsString("action=\"/schools/3/classes/7/edit\""),
                Matchers.containsString("value=\"5\""),
                Matchers.containsString("value=\"А\""),
                Matchers.containsString("Сохранить"),
                Matchers.not(Matchers.containsString("placeholder=\"Например:"))
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "create", "edit", "details"})
    void keepsSchoolNavigation(final String view) {
        final String page = XsSchoolClassesTest.page(view);
        MatcherAssert.assertThat(
            page.substring(page.indexOf("<aside"), page.indexOf("</aside>")),
            Matchers.allOf(
                Matchers.containsString("href=\"/schools/3/schedules/9\""),
                Matchers.containsString("Учебный год"),
                Matchers.containsString("href=\"/schools/3/classes\""),
                Matchers.containsString("href=\"/schools/3/buildings\""),
                Matchers.not(Matchers.containsString("/buildings/7"))
            )
        );
    }

    @Test
    void printsDetailsAndCanonicalRemoval() {
        MatcherAssert.assertThat(
            XsSchoolClassesTest.page("details"),
            Matchers.allOf(
                Matchers.containsString("Параллель:"),
                Matchers.containsString("Литера:"),
                Matchers.containsString("data-uri=\"/schools/3/classes/7\""),
                Matchers.containsString("data-redirect=\"/schools/3/classes\""),
                Matchers.containsString("href=\"/schools/3/classes/7/edit\""),
                Matchers.not(Matchers.containsString("/api/"))
            )
        );
    }

    @ParameterizedTest
    @CsvSource({"ASC, &uarr;", "DESC, &darr;", "NONE, ↕"})
    void printsSortingAndPagination(final String direction, final String icon) {
        MatcherAssert.assertThat(
            new XsClasspath("xslt/school-classes.xsl", "list-fragment").transform(
                XsSchoolClassesTest.document().replace(
                    "<direction>NONE", String.format("<direction>%s", direction)
                )
            ),
            Matchers.allOf(
                Matchers.containsString("data-sort=\"name\""),
                Matchers.containsString(icon),
                Matchers.containsString("id=\"school-classes-results\""),
                Matchers.containsString("id=\"school-classes-pagination\""),
                Matchers.containsString("data-limit=\"30\""),
                Matchers.containsString("class=\"pagination-btn pagination-next\""),
                Matchers.not(Matchers.containsString("<html"))
            )
        );
    }

    @Test
    void escapesClassNames() {
        MatcherAssert.assertThat(
            new XsClasspath("xslt/school-classes.xsl", "list-fragment").transform(
                XsSchoolClassesTest.document().replace("5А", "5&lt;script&gt;")
            ),
            Matchers.allOf(
                Matchers.containsString("5&lt;script&gt;"),
                Matchers.not(Matchers.containsString("title=\"Открыть\">5<script>")),
                Matchers.containsString("href=\"/schools/3/classes/7\""),
                Matchers.not(Matchers.containsString("/api/"))
            )
        );
    }

    private static String page(final String view) {
        return new XsClasspath("xslt/school-classes.xsl", view)
            .transform(XsSchoolClassesTest.document());
    }

    private static String document() {
        return """
            <surok>
                <school><id>3</id><name>Школа</name><schedules><schedule>
                    <id>9</id><name>Учебный год</name>
                </schedule></schedules></school>
                <error>true</error><csrf><parameter>_csrf</parameter><token>token</token></csrf>
                <classes><school-class>
                    <id>7</id><name>5А</name><grade>5</grade><litera>А</litera>
                </school-class><sorts><item><name>name</name><direction>NONE</direction>
                </item></sorts><page><offset>1</offset><limit>15</limit><total>60</total>
                    <totalPages>4</totalPages><hasNext>true</hasNext><hasPrev>false</hasPrev>
                </page></classes>
            </surok>
            """;
    }
}
