/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.teacher;

import com.eshabakhov.surok.Printable;

/**
 * School teacher printed into a supplied medium.
 *
 * @since 0.0.1
 */
public interface Teacher extends Printable {

    /**
     * Teacher identifier.
     *
     * @return Identifier
     */
    Long uid();

    /**
     * Revises the teacher using the supplied input.
     *
     * @param input Teacher input
     * @return Revised teacher
     * @throws Exception If revision fails
     */
    Teacher revise(TeacherInput input) throws Exception;
}
