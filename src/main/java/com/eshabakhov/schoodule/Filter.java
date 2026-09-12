/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * Filter parameter.
 *
 * @since 0.0.1
 */
public interface Filter {

    /**
     * Filter field name.
     * @return Field name
     */
    String name();

    /**
     * Filter value.
     * @return Value
     */
    String value();
}
