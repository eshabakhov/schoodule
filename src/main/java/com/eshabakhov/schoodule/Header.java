/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * An HTTP representation header resolved to a media implementation.
 *
 * @param <T> Printed representation type
 * @since 0.0.1
 */
@FunctionalInterface
public interface Header<T> {

    /**
     * Creates media for an endpoint representation.
     *
     * @param template Thymeleaf template
     * @param attribute Thymeleaf model attribute
     * @return Media selected by the header
     */
    Media<T> media(String template, String attribute);
}
