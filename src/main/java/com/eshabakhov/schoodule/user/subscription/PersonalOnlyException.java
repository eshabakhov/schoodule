/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user.subscription;

/**
 * Personal subscription access exception.
 *
 * @since 0.0.1
 */
final class PersonalOnlyException extends Exception {

    /**
     * New exception.
     *
     * @param user User identifier
     */
    PersonalOnlyException(final long user) {
        super(String.format("Subscriptions are unavailable for corporate user (id=%d)", user));
    }
}
