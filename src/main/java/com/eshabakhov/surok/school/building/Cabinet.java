/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Jsonable;

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

    /**
     * Returns cabinet with new name.
     *
     * @param name New name of the cabinet
     * @return Cabinet with new name
     */
    Cabinet renamed(String name);
}
