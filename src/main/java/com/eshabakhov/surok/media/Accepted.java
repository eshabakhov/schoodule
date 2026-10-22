/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import org.springframework.http.MediaType;

/**
 * Accepted response representation.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface Accepted {

    /**
     * Selects an acceptable media type.
     *
     * @return Response media type
     */
    MediaType type();
}
