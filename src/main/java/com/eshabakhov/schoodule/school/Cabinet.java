/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;

/**
 * Cabinet domain entity interface.
 *
 * @since 0.0.1
 */
public interface Cabinet extends Jsonable {

    /**
     * Returns a Cabinet unique identifier.
     *
     * @return Cabinet's ID
     */
    Long uid();

    /**
     * Returns a name of the cabinet.
     *
     * @return Name of the cabinet
     */
    String name();
}
