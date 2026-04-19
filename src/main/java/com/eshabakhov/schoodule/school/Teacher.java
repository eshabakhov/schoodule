/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;

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
}
