/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;

/**
 * School class domain entity interface.
 *
 * @since 0.0.1
 */
public interface SchoolClass extends Jsonable {

    /**
     * Returns a SchoolClass unique identifier.
     *
     * @return SchoolClass's ID
     */
    Long uid();

    /**
     * Returns a name of the schoolclass.
     *
     * @return Name of the schoolclass
     */
    String name();
}
