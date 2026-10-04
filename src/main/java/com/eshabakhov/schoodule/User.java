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

    /**
     * User credentials.
     *
     * @return Credentials
     */
    @JsonProperty
    Credentials credentials();

    /**
     * User roles.
     *
     * @return Roles
     */
    @JsonProperty
    Roles roles();

    /**
     * Reference information.
     *
     * @return Reference information
     */
    @JsonProperty
    ReferenceInformation info();

    /**
     * Administrative access marker.
     *
     * @return Administrative access marker
     */
    boolean isAdmin();
}
