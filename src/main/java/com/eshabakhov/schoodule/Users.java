/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * Users collection abstraction.
 *
 * @since 0.0.1
 */
public interface Users {

    /**
     * Register new user.
     *
     * @param username User to create
     * @param password User to create
     * @param email User to create
     * @param corporate User to create
     * @return Created user
     * @throws Exception if creation fails
     * @checkstyle ParameterNumberCheck (2 lines)
     */
    User register(
        String username,
        String password,
        String email,
        Boolean corporate
    ) throws Exception;

    /**
     * Identify user by login.
     *
     * @param login Username or email
     * @return User
     * @throws Exception if user not found
     */
    User identification(String login) throws Exception;

    /**
     * Identify user by id.
     *
     * @param id User id
     * @return User
     * @throws Exception if user not found
     */
    User identification(Long id) throws Exception;

    User authentication(String login) throws Exception;

    /**
     * Removes user by id.
     *
     * @param id User id
     * @throws Exception if user not found
     */
    void remove(Long id) throws Exception;
}
