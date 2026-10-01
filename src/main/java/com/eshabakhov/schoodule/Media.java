/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * Media that an object can print itself into.
 * Implementations define how data is rendered (JSON, Thymeleaf map, etc.).
 *
 * @since 0.0.1
 */
public interface Media {

    /**
     * Adds a string value to the media.
     *
     * @param name  Field name
     * @param value String value
     * @return Updated media
     */
    Media with(String name, String value);

    /**
     * Adds a long value to the media.
     *
     * @param name  Field name
     * @param value Long value
     * @return Updated media
     */
    Media with(String name, Long value);

    /**
     * Adds an integer value to the media.
     *
     * @param name  Field name
     * @param value Integer value
     * @return Updated media
     */
    Media with(String name, Integer value);

    /**
     * Adds a boolean value to the media.
     *
     * @param name Field name
     * @param value Boolean value
     * @return Updated media
     */
    Media with(String name, Boolean value);

    /**
     * Adds printable values to the media.
     *
     * @param name Field name
     * @param values Printable values
     * @return Updated media
     */
    Media with(String name, Iterable<? extends Printable> values);

    /**
     * Adds result pagination to the media.
     *
     * @param name Field name
     * @param value Result pagination
     * @return Updated media
     */
    Media with(String name, ResultPage value);

    /**
     * Includes specified fields.
     *
     * @param name Fields name
     * @return Updated media
     */
    Media include(String... name);
}
