/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

/**
 * An object that explicitly describes its public representation.
 *
 * <p>The object chooses the fields; the supplied medium chooses their format.
 * Implementations must use and return the medium returned by each operation:
 * the built-in media are immutable. No getters or reflective inspection of
 * domain fields are required.</p>
 *
 * @since 0.1.0
 */
@FunctionalInterface
public interface Printable {

    /**
     * Prints this representation into the supplied medium.
     *
     * @param media Initial medium
     * @param <T> Native output type of the medium
     * @return The resulting medium, never {@code null}
     * @throws Exception If the representation cannot be produced
     */
    <T> Media<T> print(Media<T> media) throws Exception;
}
