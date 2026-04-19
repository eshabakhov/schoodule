/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User credentials.
 *
 * @since 0.0.1
 */
public interface Credentials {

    /**
     * Username.
     *
     * @return Username
     */
    @JsonProperty
    String username();

    /**
     * Password.
     *
     * @return Password
     */
    String password();
}
