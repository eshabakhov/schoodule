/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.eshabakhov.schoodule.school.Cabinets;
import com.eshabakhov.schoodule.school.Schedules;
import com.eshabakhov.schoodule.school.SchoolClasses;
import com.eshabakhov.schoodule.school.Subjects;
import com.eshabakhov.schoodule.school.Teachers;

/**
 * School domain entity interface.
 *
 * <p>Represents a school entity with its properties and associated
 * collections of domain objects like cabinets, teachers, classes,
 * subjects and schedules.
 *
 * @since 0.0.1
 */
public interface School extends Jsonable {

    /**
     * Returns a School unique identifier.
     *
     * @return School's ID
     */
    Long uid();

    /**
     * Returns a name of the school.
     *
     * @return Name of the school
     */
    String name();

    /**
     * Return users.
     * @return Users
     */
    Users users();

    /**
     * Returns collection of school's cabinets.
     *
     * @return Cabinets collection
     */
    Cabinets cabinets();

    /**
     * Returns collection of school's teachers.
     *
     * @return Teachers collection
     */
    Teachers teachers();

    /**
     * Returns collection of school's classes.
     *
     * @return School classes collection
     */
    SchoolClasses schoolClasses();

    /**
     * Returns collection of school's subjects.
     *
     * @return Subjects collection
     */
    Subjects subjects();

    /**
     * Returns collection of school's schedules.
     *
     * @return Schedules collection
     */
    Schedules schedules();
}
