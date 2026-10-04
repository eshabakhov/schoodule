/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.error.SimpleError;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * User exception handler for controllers.
 *
 * <p>This class provides centralized exception handling for all controllers
 * in user's package.</p>
 *
 * @since 0.0.1
 */
@ControllerAdvice(basePackages = "com.eshabakhov.schoodule.user")
public final class UserExceptionHandler {

    /**
     * New user exception handler.
     */
    public UserExceptionHandler() {
        // Intentionally empty.
    }

    /**
     * Handle user registration failures.
     *
     * @param exception Failure
     * @return Error response
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @ExceptionHandler(
        {
            RegistrationException.class,
            RoleAssignmentException.class,
            UserCreationException.class
        }
    )
    public ResponseEntity<Object> handleRegistrationException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }
}
