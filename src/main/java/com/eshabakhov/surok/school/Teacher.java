/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.Jsonable;

/**
 * Teacher domain entity interface.
 *
 * @since 0.0.1
 */
public interface Teacher extends Jsonable {

    /**
     * Returns a Teacher unique identifier.
     *
     * @return Teacher's ID
     */
    Long uid();

    /**
     * Returns a name of the teacher.
     *
     * @return Name of the teacher
     */
    String name();

    /**
     * Returns teacher with new name.
     *
     * @param name New name of the teacher
     * @return Name of the teacher
     */
    Teacher renamed(String name);
}
