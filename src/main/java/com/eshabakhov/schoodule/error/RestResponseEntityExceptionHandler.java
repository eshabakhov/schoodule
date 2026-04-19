/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.error;

import com.eshabakhov.schoodule.controller.api.CabinetController;
import com.eshabakhov.schoodule.controller.api.SchoolController;
import com.eshabakhov.schoodule.controller.api.TeacherController;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.cabinet.CbsPostgres;
import com.eshabakhov.schoodule.school.teacher.ThsPostgres;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class provides centralized exception handling for all
 * REST controllers in the application.
 *
 * @since 0.0.1
 */
@ControllerAdvice
@SuppressWarnings(
    {
        "PMD.ProhibitPublicStaticMethods",
        "PMD.UseUtilityClass",
        "PMD.UncommentedEmptyConstructor",
        "PMD.CouplingBetweenObjects"
    }
)
public final class RestResponseEntityExceptionHandler {

    private RestResponseEntityExceptionHandler() { }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public static ResponseEntity<Object> handleMissingRequestHeaderException(
        final MissingRequestHeaderException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public static ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
        final MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage()));
    }

    @ExceptionHandler(
        {
            CabinetController.CabinetRequiredFieldException.class,
            CbsPostgres.CabinetAlreadyExistsException.class,
            SchoolController.SchoolRequiredFieldException.class,
            TeacherController.TeacherRequiredFieldException.class,
            ThsPostgres.TeacherAlreadyExistsException.class
        }
    )
    public static ResponseEntity<Object> handleClientException(final Exception exception) {
        return ResponseEntity
            .badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(
        {
            CbsPostgres.CabinetNotFoundException.class,
            SlsPostgres.SchoolNotFoundException.class,
            ThsPostgres.TeacherNotFoundException.class
        }
    )
    public static String handleNotFoundException(final Model model) {
        model.addAttribute("message", "Запрашиваемый объект не найден");
        return "error/404";
    }

    @ExceptionHandler(
        {
            CbsPostgres.CabinetFailedCreateException.class,
            CbsPostgres.CabinetFailedUpdateException.class,
            SlsPostgres.SchoolFailedCreateException.class,
            SlsPostgres.SchoolFailedUpdateException.class,
            ThsPostgres.TeacherFailedCreateException.class,
            ThsPostgres.TeacherFailedUpdateException.class
        }
    )
    public static ResponseEntity<Object> handleServerException(final Exception exception) {
        return ResponseEntity
            .internalServerError()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage()));
    }
}
