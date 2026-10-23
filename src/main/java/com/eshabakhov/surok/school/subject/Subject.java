/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.subject;

import com.eshabakhov.surok.Printable;

/**
 * School subject printed into a supplied medium.
 *
 * @since 0.0.1
 */
public interface Subject extends Printable {

    /**
     * Subject identifier.
     *
     * @return Identifier
     */
    Long uid();

    /**
     * Revises the subject using the supplied input.
     *
     * @param input Subject input
     * @return Revised subject
     * @throws Exception If revision fails
     */
    Subject revise(SubjectInput input) throws Exception;
}
