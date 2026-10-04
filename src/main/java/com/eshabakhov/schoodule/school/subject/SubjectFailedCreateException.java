/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.subject;

/**
 * Failure to create a subject.
 *
 * @since 0.0.1
 */
public final class SubjectFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public SubjectFailedCreateException() {
        super("Failed to create Subject");
    }
}
