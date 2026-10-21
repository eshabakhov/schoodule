/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.sql;

import java.util.UUID;
import java.util.stream.IntStream;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Verifies classpath SQL loading, including Unicode and untouched placeholders.
 * For example, one query can be read concurrently by request threads.
 *
 * @since 0.0.1
 */
@Timeout(5)
final class ClasspathSqlTest {

    /** Verifies UTF-8 decoding and preservation of JDBC and jOOQ placeholders. */
    @Test
    void preservesUnicodeAndQueryParameters() {
        MatcherAssert.assertThat(
            "SQL loading changed Unicode text or parameter placeholders",
            new ClasspathSql("test/unicode.sql").text(),
            Matchers.is(String.format("SELECT ? AS \"Школа O'Brien\", {0} AS parameter%c", 10))
        );
    }

    /** Verifies that a missing query fails immediately. */
    @Test
    void rejectsMissingResources() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new ClasspathSql(String.format("missing-%s.sql", UUID.randomUUID())).text(),
            "Missing SQL resource did not fail immediately"
        );
    }

    /** Verifies that concurrent requests receive the same complete SQL text. */
    @Test
    void readsResourcesConcurrently() {
        final Sql query = new ClasspathSql("test/unicode.sql");
        MatcherAssert.assertThat(
            "Concurrent SQL reads returned inconsistent content",
            IntStream.range(0, 32).parallel()
                .mapToObj(index -> query.text()).distinct().toList(),
            Matchers.contains(
                String.format("SELECT ? AS \"Школа O'Brien\", {0} AS parameter%c", 10)
            )
        );
    }
}
