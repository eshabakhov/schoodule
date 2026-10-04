/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building;

/**
 * Building already exists.
 *
 * @since 0.0.1
 */
public final class BuildingAlreadyExistsException extends Exception {

    /**
     * New exception.
     *
     * @param message Error message
     * @since 0.0.1
     */
    public BuildingAlreadyExistsException(final String message) {
        super(message);
    }
}
