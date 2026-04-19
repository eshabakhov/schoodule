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
public interface Jsonable {

    @JsonValue
    ObjectNode json();
}
