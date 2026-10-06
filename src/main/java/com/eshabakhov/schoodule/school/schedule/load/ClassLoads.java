/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.load;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.Subject;
import org.jooq.Condition;

/**
 * Class loads associated with a schedule.
 *
 * <p>Provides methods for creating, retrieving, deleting, and listing
 * subject loads assigned to school classes.</p>
 *
 * @since 0.0.1
 */
public interface ClassLoads {

    /**
     * Adds a new class load.
     *
     * @param clazz The School class,
     * @param subject The subject,
     * @param hours The hours,
     * @return The created {@link ClassLoad}
     * @throws Exception if creation fails
     */
    ClassLoad create(SchoolClass clazz, Subject subject, Integer hours) throws Exception;

    /**
     * Finds a class load by its unique ID.
     *
     * @param id Load identifier
     * @return The found {@link ClassLoad}
     * @throws Exception if not found
     */
    ClassLoad load(long id) throws Exception;

    /**
     * Lists class loads filtered by a condition.
     *
     * @param condition JOOQ condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link ClassLoad} objects
     * @throws Exception if listing fails
     */
    PageableList<ClassLoad> list(Condition condition, Page page) throws Exception;

    /**
     * Removes a class load by its ID.
     *
     * @param id Load identifier
     * @throws Exception if deletion fails
     */
    void remove(long id) throws Exception;
}
