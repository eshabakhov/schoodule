/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.error;

import com.eshabakhov.schoodule.Error;
import java.time.Instant;

/**
 * Simple implementation of {@link Error} interface.
 * <p>
 * Contains an error code, a message, and the timestamp when the error was created.
 *
 * @since 0.0.1
 */
public final class SimpleError implements Error {

    /** Human-readable error message. */
    private final String msg;

    /** Timestamp of the error creation. */
    private final Instant tsm;

    public SimpleError(final String msg) {
        this.msg = msg;
        this.tsm = Instant.now();
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
