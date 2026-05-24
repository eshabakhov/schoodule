/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;

/**
 * Subject domain entity interface.
 *
 * @since 0.0.1
 */
public interface Subject extends Jsonable {

    /**
     * Returns a Subject unique identifier.
     *
     * @return Subject's ID
     */
    Long uid();

    /**
     * Returns a name of the subject.
     *
     * @return Name of the subject
     */
    String name();

    /**
     * Returns subject with new name.
     * @param name New name of subject
     * @return Subject with new name
     */
    Subject renamed(String name);
}
