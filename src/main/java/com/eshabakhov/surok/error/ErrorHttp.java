/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.error;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servlet errors rendered without a template engine or internal exception details.
 *
 * @since 0.0.1
 */
@RestController
public final class ErrorHttp implements ErrorController {

    /** New servlet error resource. */
    public ErrorHttp() {
        // Intentionally empty.
    }

    /**
     * Render a servlet error, retaining its original status.
     *
     * @param request Error dispatch
     * @return Error response
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @RequestMapping("/error")
    public ResponseEntity<String> page(final HttpServletRequest request) {
        final Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (code instanceof Integer number) {
            final HttpStatus resolved = HttpStatus.resolve(number);
            if (resolved != null && resolved.isError()) {
                status = resolved;
            }
        }
        return new ErHttp(request, status).response();
    }
}
