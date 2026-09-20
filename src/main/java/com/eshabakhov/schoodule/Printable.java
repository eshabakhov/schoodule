/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * An object that can print itself into a {@link Media}.
 * Replaces getter-based data exposure with printer-based approach.
 *
 * @since 0.0.1
 */
public interface Printable {

    /**
     * Prints this object's data into the given media.
     *
     * @param media Target media to print into
     * @param <M> Media type
     * @return Media filled with this object's data
     */
    <M extends Media> M print(M media);
}
