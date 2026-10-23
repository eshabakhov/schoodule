/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.error;

import com.eshabakhov.surok.controller.api.ScheduleRequiredFieldException;
import com.eshabakhov.surok.federal.curriculum.input.CurriculumInputException;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RequirementInputException;
import com.eshabakhov.surok.school.SchoolFailedCreateException;
import com.eshabakhov.surok.school.SchoolNotFoundException;
import com.eshabakhov.surok.school.building.BuildingAlreadyExistsException;
import com.eshabakhov.surok.school.building.BuildingNotFoundException;
import com.eshabakhov.surok.school.building.cabinet.CabinetAlreadyExistsException;
import com.eshabakhov.surok.school.building.cabinet.CabinetFailedCreateException;
import com.eshabakhov.surok.school.building.cabinet.CabinetNotFoundException;
import com.eshabakhov.surok.school.building.cabinet.input.CabinetInputException;
import com.eshabakhov.surok.school.building.input.BuildingInputException;
import com.eshabakhov.surok.school.input.SchoolInputException;
import com.eshabakhov.surok.school.schedule.ScheduleAlreadyExistsException;
import com.eshabakhov.surok.school.schedule.ScheduleNotFoundException;
import com.eshabakhov.surok.school.schedule.load.LoadAlreadyExistsException;
import com.eshabakhov.surok.school.schedule.load.LoadFailedCreateException;
import com.eshabakhov.surok.school.schedule.load.LoadNotFoundException;
import com.eshabakhov.surok.school.schedule.load.input.LoadInputException;
import com.eshabakhov.surok.school.schoolclass.SchoolClassAlreadyExistsException;
import com.eshabakhov.surok.school.schoolclass.SchoolClassFailedCreateException;
import com.eshabakhov.surok.school.schoolclass.SchoolClassNotFoundException;
import com.eshabakhov.surok.school.schoolclass.input.SchoolClassInputException;
import com.eshabakhov.surok.school.subject.SubjectAlreadyExistsException;
import com.eshabakhov.surok.school.subject.SubjectFailedCreateException;
import com.eshabakhov.surok.school.subject.SubjectNotFoundException;
import com.eshabakhov.surok.school.subject.input.SubjectInputException;
import com.eshabakhov.surok.school.teacher.TeacherAlreadyExistsException;
import com.eshabakhov.surok.school.teacher.TeacherFailedCreateException;
import com.eshabakhov.surok.school.teacher.TeacherNotFoundException;
import com.eshabakhov.surok.school.teacher.input.TeacherInputException;
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
            SchoolInputException.class,
            BuildingInputException.class,
            CabinetInputException.class,
            ScheduleRequiredFieldException.class,
            SchoolClassInputException.class,
            SubjectInputException.class,
            TeacherInputException.class,
            CurriculumInputException.class,
            LoadInputException.class,
            RequirementInputException.class
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
            BuildingNotFoundException.class,
            CabinetNotFoundException.class,
            SchoolNotFoundException.class,
            SchoolClassNotFoundException.class,
            LoadNotFoundException.class,
            ScheduleNotFoundException.class,
            SubjectNotFoundException.class,
            TeacherNotFoundException.class
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
            BuildingAlreadyExistsException.class,
            CabinetAlreadyExistsException.class,
            ScheduleAlreadyExistsException.class,
            LoadAlreadyExistsException.class,
            SchoolClassAlreadyExistsException.class,
            SubjectAlreadyExistsException.class,
            TeacherAlreadyExistsException.class
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
            CabinetFailedCreateException.class,
            SchoolFailedCreateException.class,
            SchoolClassFailedCreateException.class,
            LoadFailedCreateException.class,
            SubjectFailedCreateException.class,
            TeacherFailedCreateException.class
        }
    )
    public static ResponseEntity<Object> handleServerException(final Exception exception) {
        return ResponseEntity
            .internalServerError()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new SimpleError(exception.getMessage(), Instant.now()));
    }
}
