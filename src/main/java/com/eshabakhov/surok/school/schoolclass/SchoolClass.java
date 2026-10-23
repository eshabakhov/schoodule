/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schoolclass;

import com.eshabakhov.surok.Printable;

/**
 * School class printed into a supplied medium.
 *
 * @since 0.0.1
 */
public interface SchoolClass extends Printable {

    /**
     * SchoolClass identifier.
     *
     * @return Identifier
     */
    Long uid();

    /**
     * Revises the class using the supplied input.
     *
     * @param input SchoolClass input
     * @return Revised class
     * @throws Exception If revision fails
     */
    SchoolClass revise(SchoolClassInput input) throws Exception;
}
