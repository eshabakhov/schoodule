/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.sql;

/**
 * SQL text that can be supplied to a parameterized DSLContext query.
 * For example, pass {@code query.text()} and bind values to {@code resultQuery}.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface Sql {

    /**
     * Reads the query without substituting its parameters.
     *
     * @return SQL text
     */
    String text();
}
