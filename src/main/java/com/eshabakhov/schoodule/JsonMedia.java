/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Media rendered as JSON.
 *
 * @since 0.0.1
 */
public interface JsonMedia extends Media {

    /**
     * Returns collected values as JSON.
     *
     * @return Collected JSON object
     */
    ObjectNode json();
}
