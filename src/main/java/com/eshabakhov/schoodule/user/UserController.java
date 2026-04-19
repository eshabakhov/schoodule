/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.User;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import org.jooq.DSLContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User management API controller.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class UserController {

    /**
     * Database context.
     */
    private final DSLContext ctx;

    UserController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping
    @Operation(summary = "Register new user")
    @ApiResponse(
        responseCode = "201",
        description = "User registered successfully",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Success",
                    value = """
                        {
                            "id": 1,
                            "username": "john_doe",
                            "email": "john@example.com"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Registration failed",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Username taken",
                    value = """
                        {
                            "message": "Username already exists",
                            "timestamp": "2025-02-02T18:00:00Z"
                        }"""
                ),
                @ExampleObject(
                    name = "Email taken",
                    value = """
                        {
                            "message": "Email already exists",
                            "timestamp": "2025-02-02T18:00:00Z"
                        }"""
                )
            }
        )
    )
    public ResponseEntity<User> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Registration request",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Regular registration",
                        value = """
                            {
                                "username": "john_doe",
                                "password": "securePassword123",
                                "email": "john@example.com"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Corporate registration",
                        value = """
                            {
                                "username": "john_doe",
                                "password": "securePassword123",
                                "email": "john@school-1.com",
                                "school": "1"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        final JsonNode username = request.get("username");
        if (username == null || username.asText().isBlank()) {
            throw new RegistrationException("Username is required");
        }
        final JsonNode password = request.get("password");
        if (password == null || password.asText().isBlank()) {
            throw new RegistrationException("Password is required");
        }
        final JsonNode email = request.get("email");
        if (email == null || email.asText().isBlank()) {
            throw new RegistrationException("Email is required");
        }
        final User created;
        final JsonNode school = request.get("school");
        if (school == null) {
            created = new UrsPostgres(this.ctx, new SlsPostgres(this.ctx).create("My school").uid())
                .register(
                    username.asText(),
                    password.asText(),
                    email.asText(),
                    false
                );
        } else {
            created = new UrsPostgres(
                this.ctx,
                new SlsPostgres(this.ctx).find(school.asLong()).uid()
            ).register(
                username.asText(),
                password.asText(),
                email.asText(),
                true
            );
        }
        return ResponseEntity
            .created(URI.create(String.format("/api/users/%s", created.uid())))
            .body(created);
    }

    /**
     * Remove user.
     *
     * @param user User's Id
     * @return Nothing
     * @throws Exception If remove fails
     */
    @DeleteMapping("/{user}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user")
    public ResponseEntity<Void> remove(@PathVariable final long user) throws Exception {
        new UrsPostgres(this.ctx).remove(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign role to user.
     *
     * @param user User's ID
     * @param request Role payload
     * @return ResponseEntity with {@link Role}
     * @throws Exception if assign fails
     */
    @PostMapping("/{user}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user")
    @ApiResponse(
        responseCode = "201",
        description = "Role assigned successfully",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Success",
                    value = """
                        {
                            "id": 1,
                            "userId": 5,
                            "role": "TEACHER",
                            "schoolId": 3
                        }"""
                )
            }
        )
    )
    public ResponseEntity<Role> assignRole(
        @PathVariable final long user,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Role assignment request",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Assign teacher role",
                        value = """
                            {
                                "role": "TEACHER"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Assign admin role",
                        value = """
                            {
                                "role": "ADMIN"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        final JsonNode rolenode = request.get("role");
        if (rolenode == null || rolenode.asText().isBlank()) {
            throw new RoleAssignmentException("Role is required");
        }
        final var assigned = new UrsPostgres(this.ctx)
            .identification(user)
            .roles()
            .grant(
                switch (rolenode.asText()) {
                    case "ADMIN" -> RoleEnum.ADMIN;
                    case "DIRECTOR" -> RoleEnum.DIRECTOR;
                    case "DEPUTY_DIRECTOR" -> RoleEnum.DEPUTY_DIRECTOR;
                    case "TEACHER" -> RoleEnum.TEACHER;
                    default -> RoleEnum.STUDENT;
                }
            );
        return ResponseEntity
            .created(
                URI.create(
                    String.format(
                        "/api/users/%d/roles/%d",
                        user,
                        assigned.uid()
                    )
                )
            )
            .body(assigned);
    }

    /**
     * Retrieve list of user's role.
     *
     * @param user User's Id
     * @return ResponseEntity with list of {@link Role}
     */
    @GetMapping("/{user}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all roles for user")
    public ResponseEntity<List<Role>> listRoles(@PathVariable final long user) throws Exception {
        return ResponseEntity.ok(
            new UrsPostgres(this.ctx)
                .identification(user)
                .roles()
                .list()
        );
    }

    /**
     * Remove user's role.
     *
     * @param user User's Id
     * @param role Role
     * @return Nothing
     * @throws Exception If delete fails
     */
    @DeleteMapping("/{user}/roles/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user")
    public ResponseEntity<Void> removeRole(
        @PathVariable final long user,
        @PathVariable final long role
    ) throws Exception {
        new UrsPostgres(this.ctx)
            .identification(user)
            .roles()
            .revoke(role);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registration exception.
     *
     * @since 0.0.1
     */
    public static class RegistrationException extends Exception {

        /**
         * Constructor.
         *
         * @param message Error message
         */
        public RegistrationException(final String message) {
            super(message);
        }
    }

    /**
     * Role assignment exception.
     *
     * @since 0.0.1
     */
    public static class RoleAssignmentException extends Exception {

        public RoleAssignmentException(final String message) {
            super(message);
        }
    }
}
