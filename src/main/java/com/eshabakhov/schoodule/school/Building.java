/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;
import com.eshabakhov.schoodule.school.building.Cabinets;

/**
 * Building domain entity interface.
 *
 * @since 0.0.1
 */
public interface Building extends Jsonable {

    /**
     * Returns a Building unique identifier.
     *
     * @return Building's ID
     */
    Long uid();

    /**
     * Returns a name of the building.
     *
     * @return Name of the building
     */
    String name();

    /**
     * Returns building with new name.
     * @param name New name of the building
     * @return Building with new name
     */
    Building renamed(String name);

    /**
     * Returns collection of cabinets.
     *
     * @return Cabinets collection
     */
    Cabinets cabinets();
}
