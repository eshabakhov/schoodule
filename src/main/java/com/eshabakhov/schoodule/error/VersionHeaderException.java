/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.error;

/**
 * Exception for version header.
 *
 * @since 0.0.1
 */
public class VersionHeaderException extends Exception {
    public VersionHeaderException(final String header) {
        super(header);
    }
}
