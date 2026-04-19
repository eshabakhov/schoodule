/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.user.Credentials;
import com.eshabakhov.schoodule.user.ReferenceInformation;
import com.eshabakhov.schoodule.user.Roles;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User abstraction.
 *
 * @since 0.0.1
 */
public interface User {

    /**
     * User unique identifier.
     *
     * @return User ID
     */
    @JsonProperty
    long uid();

    @JsonProperty
    Credentials credentials();

    @JsonProperty
    Roles roles();

    @JsonProperty
    ReferenceInformation info();

    boolean isAdmin();
}
