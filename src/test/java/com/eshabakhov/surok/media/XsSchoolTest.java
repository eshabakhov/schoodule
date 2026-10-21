/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * School XSLT fragments render collection items safely.
 *
 * @since 0.0.1
 */
final class XsSchoolTest {

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
}
