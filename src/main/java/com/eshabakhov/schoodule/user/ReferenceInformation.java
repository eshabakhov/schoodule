/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.User;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reference information for {@link User}.
 *
 * @since 0.0.1
 */
public interface ReferenceInformation {

    @JsonProperty
    Long school();

    @JsonProperty
    Boolean corporate();

    @JsonProperty
    Boolean alive();
}
