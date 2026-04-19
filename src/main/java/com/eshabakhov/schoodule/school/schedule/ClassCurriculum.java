/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule;

import com.eshabakhov.schoodule.Jsonable;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.schedule.curriculum.AssignedClassCurriculum;

/**
 * Class curriculum domain entity interface.
 *
 * <p>Represents a curriculum plan for a specific class and subject,
 * defining how many hours per week should be allocated.
 *
 * @since 0.0.1
 */
public interface ClassCurriculum extends Jsonable {

    /**
     * Returns a ClassCurriculum unique identifier.
     *
     * @return ClassCurriculum's ID
     */
    Long uid();

    /**
     * Returns the school class.
     *
     * @return School class
     */
    SchoolClass schoolClass();

    /**
     * Returns the subject.
     *
     * @return Subject
     */
    Subject subject();

    /**
     * Returns the number of hours per week planned for this subject.
     *
     * @return Hours per week
     */
    Integer hoursPerWeek();

    AssignedClassCurriculum assignedCurriculum();
}
