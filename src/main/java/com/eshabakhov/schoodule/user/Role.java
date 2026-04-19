/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User role assignment abstraction.
 *
 * @since 0.0.1
 */
public interface Role {

    /**
     * User role assignment unique identifier.
     *
     * @return UserRole ID
     */
    @JsonProperty
    long uid();

    /**
     * Role name.
     *
     * @return Role name
     */
    @JsonProperty
    String name();
}
