/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

/**
 * SQL resource loaded from the classpath, including an executable application JAR.
 * For example, {@code new ClasspathSql("school/create.sql").text()} reads a query
 * under {@code src/main/resources/sql} without depending on the working directory.
 *
 * @since 0.0.1
 */
public final class ClasspathSql implements Sql {

    /** Query path relative to the classpath SQL directory. */
    private final String path;

    /**
     * Creates a reference to a classpath query.
     *
     * @param path Query path relative to {@code sql/}
     */
    public ClasspathSql(final String path) {
        this.path = path;
    }

    @Override
    public String text() {
        try (
            InputStream stream = new ClassPathResource(
                String.format("sql/%s", this.path)
            ).getInputStream()
        ) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException failure) {
            throw new IllegalStateException(
                String.format("Unable to read SQL resource 'sql/%s'", this.path),
                failure
            );
        }
    }
}
