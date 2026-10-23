/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Printable;

/**
 * Teaching loads belonging to a schedule.
 *
 * @since 0.0.1
 */
public interface ClassLoads extends Printable {

    /**
     * Creates a teaching load.
     *
     * @param input Requested load fields
     * @return Created load
     * @throws Exception If creation fails
     */
    ClassLoad create(ClassLoadInput input) throws Exception;

    /**
     * Selects a load in this schedule.
     *
     * @param id Load identifier
     * @return Selected load
     * @throws Exception If the load is absent
     */
    ClassLoad load(long id) throws Exception;

    /**
     * Revises a load in this schedule.
     *
     * @param id Load identifier
     * @param input Requested fields
     * @return Revised load
     * @throws Exception If revision fails
     */
    default ClassLoad revise(final long id, final ClassLoadInput input) throws Exception {
        return this.load(id).revise(input);
    }

    /**
     * Selects a page of loads.
     *
     * @param page Requested page
     * @return Selected loads
     * @throws Exception If selection fails
     */
    ClassLoads selection(Page page) throws Exception;

    /**
     * Iterates over represented loads.
     *
     * @return Loads
     */
    Iterable<ClassLoad> iterate();

    /**
     * Removes a load from this schedule.
     *
     * @param id Load identifier
     * @throws Exception If removal fails
     */
    void remove(long id) throws Exception;
}
