/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building;

/**
 * Building was not found.
 *
 * @since 0.0.1
 */
public final class BuildingNotFoundException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public BuildingNotFoundException(final String message) {
        super(message);
    }
}
