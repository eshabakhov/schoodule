/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import org.jooq.Condition;

/**
 * Interface for managing {@link SchoolClass} entities.
 * <p>
 * Defines operations for creating, retrieving, updating, deleting,
 * listing, and counting school classes.
 *
 * @since 0.0.1
 */
public interface SchoolClasses {

    /**
     * Creates a new school class with the given name.
     *
     * @param litter The school class litter
     * @param grade The school class grade
     * @return The created {@link SchoolClass}
     */
    SchoolClass add(String litter, Integer grade) throws Exception;

    /**
     * Finds a school class by its unique identifier.
     *
     * @param id The class ID
     * @return The found {@link SchoolClass}
     */
    SchoolClass clazz(long id) throws Exception;

    /**
     * Returns a paginated list of school classes optionally filtered by a search string.
     *
     * @param condition Jooq condition for filtering
     * @param page Pagination (contains limit and offset)
     * @return List of {@link SchoolClass} instances
     */
    PageableList<SchoolClass> classes(Condition condition, Page page) throws Exception;

    /**
     * Updates the name of an existing school class.
     *
     * @param id ID School class
     * @param litter The school class litter
     * @param grade The school class grade
     * @return The updated {@link SchoolClass}
     */
    SchoolClass put(Long id, String litter, Integer grade) throws Exception;

    /**
     * Removes a school class by its ID.
     *
     * @param id The school class ID
     */
    void remove(long id) throws Exception;
}
