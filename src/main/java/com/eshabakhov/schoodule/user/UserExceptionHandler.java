/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.error.SimpleError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * User exception handler for controllers.
 *
 * <p>This class provides centralized exception handling for all controllers in user's package.
 *
 * @since 0.0.1
 */
@ControllerAdvice(basePackages = "com.eshabakhov.schoodule.user")
public final class UserExceptionHandler {

    private UserExceptionHandler() { }

    @ExceptionHandler(
        {
            UserController.RegistrationException.class,
            UserController.RoleAssignmentException.class,
            UrsPostgres.UserCreationException.class
        }
    )
    public static ResponseEntity<Object> handleRegistrationException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage()));
    }
}
