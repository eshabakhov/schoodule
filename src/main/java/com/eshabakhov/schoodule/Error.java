/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/**
 * Represents an error response with code, message, and timestamp.
 *
 * @since 0.0.1
 */
public interface Error {

    /**
     * Human-readable error message.
     * @return Message
     */
    @JsonProperty
    String message();

    /**
     * Timestamp of the error occurrence.
     * @return Timestamp
     */
    @JsonProperty
    Instant timestamp();
}
