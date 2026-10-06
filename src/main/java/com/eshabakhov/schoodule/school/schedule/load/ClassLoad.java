/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

import com.eshabakhov.schoodule.Jsonable;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;

/**
 * Teaching load assigned to a school class for a subject.
 *
 * <p>Defines how many hours per week a specific class studies a subject.</p>
 *
 * @since 0.0.1
 */
public interface ClassLoad extends Jsonable {

    /**
     * Returns the unique identifier of this class load.
     *
     * @return Class load identifier
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

    /**
     * Returns a new load assigned to the specified subject.
     *
     * @param subject New subject
     * @return New load with changed subject
     */
    ClassLoad teach(Subject subject);

    /**
     * Returns a new load assigned to the specified school class.
     *
     * @param clazz New school class
     * @return New load with changed school class
     */
    ClassLoad target(SchoolClass clazz);

    /**
     * Returns a new load with allocated weekly hours.
     *
     * @param hours New amount of hours per week
     * @return New load with changed weekly hours
     */
    ClassLoad allocate(Integer hours);
}
