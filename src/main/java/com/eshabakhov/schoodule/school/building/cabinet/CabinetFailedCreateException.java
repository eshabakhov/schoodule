/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building.cabinet;

/**
 * Failure to create a cabinet.
 *
 * @since 0.0.1
 */
public final class CabinetFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public CabinetFailedCreateException() {
        super("Failed to create Cabinet");
    }
}
