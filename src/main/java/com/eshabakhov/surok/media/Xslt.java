/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

/**
 * XSLT transformation.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface Xslt {

    /**
     * Transforms XML source.
     *
     * @param xml XML document
     * @return Transformed document
     */
    String transform(String xml);
}
