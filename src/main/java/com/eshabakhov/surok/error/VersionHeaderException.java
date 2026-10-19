/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.error;

/**
 * Exception for version header.
 *
 * @since 0.0.1
 */
public class VersionHeaderException extends Exception {

    /**
     * New exception.
     *
     * @param header Unsupported version header
     */
    public VersionHeaderException(final String header) {
        super(header);
    }
}
