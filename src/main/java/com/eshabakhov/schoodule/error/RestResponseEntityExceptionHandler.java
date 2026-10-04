/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.error;

import com.eshabakhov.schoodule.controller.api.CabinetRequiredFieldException;
import com.eshabakhov.schoodule.controller.api.ScheduleRequiredFieldException;
import com.eshabakhov.schoodule.controller.api.SchoolClassRequiredFieldException;
import com.eshabakhov.schoodule.controller.api.SchoolRequiredFieldException;
import com.eshabakhov.schoodule.controller.api.SubjectRequiredFieldException;
import com.eshabakhov.schoodule.controller.api.TeacherRequiredFieldException;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.building.cabinet.CbsPostgres;
import com.eshabakhov.schoodule.school.schedule.SdsPostgres;
import com.eshabakhov.schoodule.school.schoolclass.ScsPostgres;
import com.eshabakhov.schoodule.school.subject.SbsPostgres;
import com.eshabakhov.schoodule.school.teacher.ThsPostgres;
import java.time.Instant;
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
 * REST controllers in the application.</p>
 *
 * @since 0.0.1
 */
@ControllerAdvice
@SuppressWarnings(
    "PMD.ProhibitPublicStaticMethods"
)
public final class RestResponseEntityExceptionHandler {

    private RestResponseEntityExceptionHandler() { }

    /**
     * Handle a missing request header.
     *
     * @param exception Missing header exception
     * @return Bad request response
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public static ResponseEntity<Object> handleMissingRequestHeaderException(
        final MissingRequestHeaderException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }

    /**
     * Handle an invalid client request.
     *
     * @param exception Client request exception
     * @return Bad request response
     */
    @ExceptionHandler(
        {
            SchoolRequiredFieldException.class,
            CabinetRequiredFieldException.class,
            ScheduleRequiredFieldException.class,
            SchoolClassRequiredFieldException.class,
            SubjectRequiredFieldException.class,
            TeacherRequiredFieldException.class
        }
    )
    public static ResponseEntity<Object> handleClientException(final Exception exception) {
        return ResponseEntity
            .badRequest()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }

    /**
     * Handle a missing resource.
     *
     * @param model View model
     * @return Not-found template name
     */
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

    /**
     * Handle an invalid method argument type.
     *
     * @param exception Type mismatch exception
     * @return Not acceptable response
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public static ResponseEntity<Object> handleMethodArgumentTypeMismatchException(
        final MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }

    /**
     * Handle a resource conflict.
     *
     * @param exception Conflict exception
     * @return Conflict response
     */
    @ExceptionHandler(
        {
            ScsPostgres.SchoolClassAlreadyExistsException.class,
            CbsPostgres.CabinetAlreadyExistsException.class,
            SdsPostgres.ScheduleAlreadyExistsException.class,
            ScsPostgres.SchoolClassAlreadyExistsException.class,
            SbsPostgres.SubjectAlreadyExistsException.class,
            ThsPostgres.TeacherAlreadyExistsException.class
        }
    )
    public static ResponseEntity<Object> handleConflictException(final Exception exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }

    /**
     * Handle an internal server error.
     *
     * @param exception Server exception
     * @return Internal server error response
     */
    @ExceptionHandler(
        {
            CbsPostgres.CabinetFailedCreateException.class,
            SlsPostgres.SchoolFailedCreateException.class,
            ThsPostgres.TeacherFailedCreateException.class
        }
    )
    public static ResponseEntity<Object> handleServerException(final Exception exception) {
        return ResponseEntity
            .internalServerError()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }
}
