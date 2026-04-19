/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.school.schedule.curriculum;

import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;

/**
 * Class curriculum domain entity interface.
 *
 * <p>Represents a curriculum plan for a specific class and subject,
 * defining how many hours per week should be allocated.
 *
 * @since 0.0.1
 */
public interface AssignedClassCurriculum extends ClassCurriculum {

    /**
     * Returns the number of hours per week planned for this subject.
     *
     * @return Hours per week
     */
    Integer assignedHours();
}
