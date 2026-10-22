/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests {@link XsClasspath} transformations of school pages.
 * For example, school forms and building fragments retain safe navigation.
 *
 * @since 0.0.1
 */
final class XsClasspathTest {

    /** Verifies fragments do not contain the page layout. */
    @Test
    void escapesSchoolNamesInListFragments() {
        MatcherAssert.assertThat(
            "School fragment exposed markup or included page chrome",
            new XsClasspath("xslt/schools.xsl", "list-fragment").transform(
                """
                <surok><schools><school><id>7</id><name>Школа &lt;script&gt;</name></school>\
                </schools></surok>\
                """
            ),
            Matchers.allOf(
                Matchers.containsString("Школа &lt;script&gt;"),
                Matchers.not(Matchers.containsString("<html"))
            )
        );
    }

    /**
     * Verifies both forms use the shared CSRF input.
     *
     * @param view Selected form
     */
    @ParameterizedTest
    @ValueSource(strings = {"create", "edit"})
    void includesCsrfInSchoolForms(final String view) {
        final String stylesheet;
        if ("create".equals(view)) {
            stylesheet = "xslt/schools.xsl";
        } else {
            stylesheet = "xslt/school.xsl";
        }
        MatcherAssert.assertThat(
            "School form lost its CSRF field",
            new XsClasspath(stylesheet, view).transform(
                """
                <surok>
                    <csrf><parameter>_csrf</parameter><token>токен</token></csrf>
                    <schools><school><id>7</id><name>Лицей</name></school></schools>
                </surok>
                """
            ),
            Matchers.allOf(
                Matchers.containsString("name=\"_csrf\""),
                Matchers.containsString("value=\"токен\"")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"building", "cabinet"})
    void escapesNamesWithoutIncludingPageChrome(final String entity) {
        MatcherAssert.assertThat(
            "Fragment lost its data, exposed markup or included a full page",
            new XsClasspath(String.format("xslt/%ss.xsl", entity), "list-fragment").transform(
                String.format(
                    """
                    <surok>
                        <school><id>3</id><name>Школа</name></school>
                        <building><id>4</id><name>Корпус</name></building>
                        <%1$ss><%1$s><id>7</id>
                        <name>Зал &lt;script&gt;проверка&lt;/script&gt;</name></%1$s>
                        <page><offset>1</offset><limit>15</limit>
                        <total>1</total><totalPages>1</totalPages>
                        <hasNext>false</hasNext><hasPrev>false</hasPrev></page></%1$ss>
                    </surok>
                    """, entity
                )
            ),
            Matchers.allOf(
                Matchers.containsString("Зал &lt;script&gt;проверка&lt;/script&gt;"),
                Matchers.containsString("data-id=\"7\""),
                Matchers.not(Matchers.containsString("<html"))
            )
        );
    }

    @ParameterizedTest
    @CsvSource({"building, list", "building, create", "cabinet, list", "cabinet, create"})
    void keepsCollectionRowsOutOfSelectedNavigation(final String entity, final String view) {
        final String printed = XsClasspathTest.page(entity, view, false);
        MatcherAssert.assertThat(
            "Collection navigation selected a row or lost school schedules",
            printed.substring(printed.indexOf("<aside"), printed.indexOf("</aside>")),
            Matchers.allOf(
                Matchers.containsString("href=\"/schools/3/schedules/5\""),
                Matchers.containsString("Первое полугодие"),
                Matchers.not(Matchers.containsString("Южный зал"))
            )
        );
    }

    @ParameterizedTest
    @CsvSource({"building, details", "building, edit", "cabinet, details", "cabinet, edit"})
    void keepsSelectedNavigationOnEntityPages(final String entity, final String view) {
        final String printed = XsClasspathTest.page(entity, view, false);
        MatcherAssert.assertThat(
            "Entity pages lost the selected building or cabinet in navigation",
            printed.substring(printed.indexOf("<aside"), printed.indexOf("</aside>")),
            Matchers.containsString("Южный зал")
        );
    }

    @ParameterizedTest
    @CsvSource({"building, 'Например: Корпус 1'", "cabinet, 'Например: Кабинет 301'"})
    void keepsCreationFormsEmpty(final String entity, final String example) {
        MatcherAssert.assertThat(
            "Creation form lost its guidance or copied an existing name",
            XsClasspathTest.page(entity, "create", false),
            Matchers.allOf(
                Matchers.containsString("Заполните данные для создания"),
                Matchers.containsString(String.format("placeholder=\"%s\"", example)),
                Matchers.containsString("value=\"\""),
                Matchers.not(Matchers.containsString("value=\"Южный зал\"")),
                Matchers.not(Matchers.containsString("class=\"error-message\""))
            )
        );
    }

    @ParameterizedTest
    @CsvSource({"building, create", "building, edit", "cabinet, create", "cabinet, edit"})
    void printsValidationErrorsOnForms(final String entity, final String view) {
        MatcherAssert.assertThat(
            "Invalid form submissions must show the existing validation message",
            XsClasspathTest.page(entity, view, true),
            Matchers.allOf(
                Matchers.containsString("class=\"error-message\""),
                Matchers.containsString("Название не может быть пустым"),
                Matchers.containsString("name=\"_csrf\" value=\"token\"")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"building", "cabinet"})
    void keepsEditingValues(final String entity) {
        MatcherAssert.assertThat(
            "Editing must keep the current name without creation guidance",
            XsClasspathTest.page(entity, "edit", false),
            Matchers.allOf(
                Matchers.containsString("value=\"Южный зал\""),
                Matchers.not(Matchers.containsString("placeholder=\"Например:")),
                Matchers.containsString("Сохранить")
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"building", "cabinet"})
    void keepsPaginationControls(final String entity) {
        MatcherAssert.assertThat(
            "Collection fragments lost page navigation or page-size choices",
            XsClasspathTest.page(entity, "list-fragment", false),
            Matchers.allOf(
                Matchers.containsString("class=\"pagination-btn pagination-prev\""),
                Matchers.containsString("class=\"pagination-btn pagination-next\""),
                Matchers.containsString("class=\"pagination-btn active\""),
                Matchers.containsString("data-limit=\"15\""),
                Matchers.containsString("data-limit=\"30\""),
                Matchers.containsString("data-limit=\"50\""),
                Matchers.containsString("class=\"pagination-ellipsis\"")
            )
        );
    }

    @ParameterizedTest
    @CsvSource({
        "building, ASC, &uarr;", "building, DESC, &darr;", "building, NONE, ↕",
        "cabinet, ASC, &uarr;", "cabinet, DESC, &darr;", "cabinet, NONE, ↕"
    })
    void showsNameSorting(final String entity, final String direction, final String icon) {
        String active = "";
        if (!"NONE".equals(direction)) {
            active = " active";
        }
        for (final String view : List.of("list", "list-fragment")) {
            MatcherAssert.assertThat(
                new XsClasspath(String.format("xslt/%ss.xsl", entity), view).transform(
                    XsClasspathTest.document(entity, false).replace(
                        String.format("</%ss>", entity),
                        String.format(
                            """
                            <sorts><item><name>name</name><direction>%s</direction></item>
                            </sorts></%ss>
                            """, direction, entity
                        )
                    )
                ),
                Matchers.allOf(
                    Matchers.containsString("data-sort=\"name\""),
                    Matchers.containsString(
                        String.format("class=\"%ss-sort%s\"", entity, active)
                    ),
                    Matchers.containsString(
                        String.format("class=\"%ss-sort-icon\">%s</span>", entity, icon)
                    ),
                    Matchers.not(Matchers.containsString("data-sort=\"id\""))
                )
            );
        }
    }

    private static String page(final String entity, final String view, final boolean error) {
        return new XsClasspath(String.format("xslt/%ss.xsl", entity), view)
            .transform(XsClasspathTest.document(entity, error));
    }

    private static String document(final String entity, final boolean error) {
        String building = "";
        if ("cabinet".equals(entity)) {
            building = "<building><id>4</id><name>Учебный корпус</name></building>";
        }
        return String.format(
            """
            <surok>
                <school><id>3</id><name>Школа</name><schedules><schedule>
                <id>5</id><name>Первое полугодие</name></schedule></schedules></school>
                %2$s<error>%3$s</error>
                <csrf><parameter>_csrf</parameter><token>token</token></csrf>
                <%1$ss><%1$s><id>7</id><name>Южный зал</name></%1$s>
                <page><offset>5</offset><limit>15</limit><total>150</total>
                <totalPages>10</totalPages><hasNext>true</hasNext>
                <hasPrev>true</hasPrev></page></%1$ss>
            </surok>
            """, entity, building, error
        );
    }
}
