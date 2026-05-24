/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Jsonable;

/**
 * School class domain entity interface.
 *
 * @since 0.0.1
 */
public interface SchoolClass extends Jsonable {

    /**
     * Returns a SchoolClass unique identifier.
     *
     * @return SchoolClass's ID
     */
    Long uid();

    /**
     * Returns a name of the schoolclass.
     *
     * @return Name of the schoolclass
     */
    String name();

    /**
     * Returns a grade of the schoolclass.
     *
     * @return Grade of the schoolclass
     */
    Integer grade();

    /**
     * Returns a litera of the schoolclass.
     *
     * @return Litera of the schoolclass
     */
    String litera();

    /**
     * Returns schoolclass with new grade.
     * @param grade New grade of schoolclass
     * @return Schoolclass with new grade
     */
    SchoolClass regraded(Integer grade);

    /**
     * Returns schoolclass with new litera.
     * @param litera New litera of schoolclass
     * @return Schoolclass with new litera.
     */
    SchoolClass reliterated(String litera);
}
