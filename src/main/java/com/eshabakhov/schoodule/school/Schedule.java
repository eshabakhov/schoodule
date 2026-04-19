/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculums;

/**
 * Schedule domain entity interface.
 *
 * @since 0.0.1
 */
public interface Schedule extends Jsonable {

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

    ClassCurriculums curriculums();
}
