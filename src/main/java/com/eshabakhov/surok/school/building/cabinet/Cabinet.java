/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.Printable;

/**
 * Building cabinet printed into a supplied medium.
 *
 * @since 0.0.1
 */
public interface Cabinet extends Printable {

    /**
     * Cabinet identifier.
     *
     * @return Identifier
     */
    Long uid();

    /**
     * Revises the cabinet using the supplied input.
     *
     * @param input Cabinet input
     * @return Revised cabinet
     * @throws Exception If revision fails
     */
    Cabinet revise(CabinetInput input) throws Exception;
}
