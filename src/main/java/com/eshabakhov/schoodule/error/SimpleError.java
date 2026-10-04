/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.error;

import com.eshabakhov.schoodule.Error;
import java.time.Instant;

/**
 * Simple implementation of {@link Error} interface.
 *
 * <p>Contains a message and the timestamp when the error was created.</p>
 *
 * @since 0.0.1
 */
public final class SimpleError implements Error {

    /** Human-readable error message. */
    private final String msg;

    /** Timestamp of the error creation. */
    private final Instant tsm;

    /**
     * New error.
     *
     * @param msg Error message
     * @param tsm Error creation timestamp
     */
    public SimpleError(final String msg, final Instant tsm) {
        this.msg = msg;
        this.tsm = tsm;
    }

    @Override
    public String message() {
        return this.msg;
    }

    @Override
    public Instant timestamp() {
        return this.tsm;
    }
}
