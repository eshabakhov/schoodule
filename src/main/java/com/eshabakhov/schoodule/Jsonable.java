/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON representation.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface Jsonable {

    /**
     * JSON representation.
     *
     * @return JSON object
     */
    @JsonValue
    ObjectNode json();
}
