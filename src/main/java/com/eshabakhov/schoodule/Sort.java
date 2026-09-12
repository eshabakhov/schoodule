/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * Sorting parameter.
 *
 * @since 0.0.1
 */
public interface Sort {

    /**
     * Sorting field name.
     * @return Field name
     */
    String name();

    /**
     * Sorting direction.
     * @return Direction
     */
    Direction direction();

    /**
     * Sorting direction.
     *
     * @since 0.0.1
     */
    enum Direction {
        /**
         * Sorting is not applied.
         */
        NONE,

        /**
         * Ascending sorting.
         */
        ASC,

        /**
         * Descending sorting.
         */
        DESC
    }
}
