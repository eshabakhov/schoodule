/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * User roles collection abstraction.
 *
 * @since 0.0.1
 */
public interface Roles {

    /**
     * Assign role to user.
     *
     * @param role User role
     * @return Created user role
     * @throws Exception if assignment fails
     */
    Role grant(RoleEnum role) throws Exception;

    /**
     * Get all roles for user.
     *
     * @return List of user roles
     */
    @JsonProperty
    List<Role> list();

    /**
     * Remove role from user.
     *
     * @param identity User role ID
     * @throws Exception if removal fails
     */
    void revoke(long identity) throws Exception;
}
