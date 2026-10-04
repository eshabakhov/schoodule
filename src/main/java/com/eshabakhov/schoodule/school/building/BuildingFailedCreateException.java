/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.building;

/**
 * Failure to create a building.
 *
 * @since 0.0.1
 */
public final class BuildingFailedCreateException extends Exception {

    /**
     * New exception.
     *
     * @since 0.0.1
     */
    public BuildingFailedCreateException() {
        super("Failed to create Building");
    }
}
