/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.Jsonable;
import com.eshabakhov.surok.school.schedule.load.ClassLoads;

/**
 * Schedule domain entity interface.
 *
 * @since 0.0.1
 */
public interface Schedule extends Jsonable {

    /**
     * Returns a Schedule unique identifier.
     *
     * @return Schedule's ID
     */
    Long uid();

    /**
     * Returns a name of the schedule.
     *
     * @return Name of the schedule
     */
    String name();

    /**
     * Returns schedule with new name.
     *
     * @param name New name of schedule
     * @return Schedule with new name
     */
    Schedule renamed(String name);

    /**
     * Returns class loads.
     *
     * @return Class loads
     */
    ClassLoads loads();
}
