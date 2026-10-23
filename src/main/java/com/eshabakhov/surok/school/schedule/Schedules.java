/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.Sorts;

/**
 * Interface for managing {@link Schedule} entities.
 *
 * @since 0.0.1
 */
public interface Schedules extends Printable {

    /**
     * Creates a new school schedule.
     *
     * @param input School schedule input
     * @return The created {@link Schedule}
     * @throws Exception if creation fails
     */
    Schedule create(ScheduleInput input) throws Exception;

    /**
     * Finds a school schedule by its unique identifier.
     *
     * @param id The school schedule ID
     * @return The found {@link Schedule}
     * @throws Exception if not found
     */
    Schedule schedule(long id) throws Exception;

    /**
     * Revises a school schedule.
     *
     * @param id The school schedule ID
     * @param input School schedule input
     * @return Revised school schedule
     * @throws Exception if revision fails
     */
    default Schedule revise(
        final long id,
        final ScheduleInput input
    ) throws Exception {
        return this.schedule(id).revise(input);
    }

    /**
     * Returns a paginated list of school schedules.
     *
     * @param filters Filters for selecting school schedules
     * @param page Pagination (contains limit and offset)
     * @param sorts Sorting parameters
     * @return List of {@link Schedule} instances
     * @throws Exception if listing fails
     */
    Schedules selection(Filters filters, Page page, Sorts sorts)
        throws Exception;

    /**
     * Iterates over school schedules represented by this collection.
     *
     * @return School schedules
     */
    Iterable<Schedule> iterate();

    /**
     * Removes a school schedule by its ID.
     *
     * @param id The school schedule ID
     * @throws Exception if removal fails
     */
    void remove(long id) throws Exception;
}
