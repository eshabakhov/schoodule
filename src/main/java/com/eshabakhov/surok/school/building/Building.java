/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.school.building.cabinet.Cabinets;

/**
 * School building printed into a supplied medium.
 *
 * @since 0.0.1
 */
public interface Building extends Printable {

    /**
     * Building identifier.
     *
     * @return Identifier
     */
    Long uid();

    /**
     * Revises the building using the supplied input.
     *
     * @param input Building input
     * @return Revised building
     * @throws Exception If revision fails
     */
    Building revise(BuildingInput input) throws Exception;

    /**
     * Cabinets belonging to this building.
     *
     * @return Cabinets
     */
    Cabinets cabinets();
}
