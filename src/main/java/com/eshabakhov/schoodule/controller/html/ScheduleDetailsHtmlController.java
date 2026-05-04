/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
import com.eshabakhov.schoodule.tables.LessonAssignment;
import com.eshabakhov.schoodule.tables.LessonSlot;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for Html response {@link School} details.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}")
public class ScheduleDetailsHtmlController {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    ScheduleDetailsHtmlController(final DSLContext datasource) {
        this.datasource = datasource;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long schedule
    ) throws Exception {
        final School sch = new SlsPostgres(this.datasource).find(school);
        final var sched = sch.schedules().find(schedule);
        return new ModelAndView("schedules/details")
            .addAllObjects(
                Map.of(
                "school", sch,
                "schedule", sched,
                "classCount", sch.schoolClasses()
                    .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
                    .total(),
                "lessonCount", this.datasource
                    .select(DSL.count())
                    .from(LessonSlot.LESSON_SLOT)
                    .join(LessonAssignment.LESSON_ASSIGNMENT)
                    .on(
                        LessonSlot.LESSON_SLOT.LESSON_ASSIGNMENT_ID
                            .eq(LessonAssignment.LESSON_ASSIGNMENT.ID)
                    )
                    .join(ClassCurriculum.CLASS_CURRICULUM)
                    .on(
                        LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID
                            .eq(ClassCurriculum.CLASS_CURRICULUM.ID)
                    )
                    .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
                    .fetchOne(0, Integer.class),
            "cabinetCount", this.datasource
                .select(DSL.countDistinct(LessonSlot.LESSON_SLOT.CABINET_ID))
                .from(LessonSlot.LESSON_SLOT)
                .join(LessonAssignment.LESSON_ASSIGNMENT)
                .on(
                    LessonSlot.LESSON_SLOT.LESSON_ASSIGNMENT_ID
                        .eq(LessonAssignment.LESSON_ASSIGNMENT.ID)
                )
                .join(ClassCurriculum.CLASS_CURRICULUM)
                .on(
                    LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID
                        .eq(ClassCurriculum.CLASS_CURRICULUM.ID)
                )
                .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
                .fetchOne(0, Integer.class),
                "teacherCount", this.datasource
                    .select()
                    .from(LessonAssignment.LESSON_ASSIGNMENT)
                    .join(ClassCurriculum.CLASS_CURRICULUM)
                    .on(
                        LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID
                            .eq(ClassCurriculum.CLASS_CURRICULUM.ID)
                    )
                    .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
                    .fetch()
                    .stream()
                    .map(r -> r.get(LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID))
                    .distinct()
                    .count(),
                "subjectCount", this.datasource
                    .select(DSL.countDistinct(ClassCurriculum.CLASS_CURRICULUM.SUBJECT_ID))
                    .from(ClassCurriculum.CLASS_CURRICULUM)
                    .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
                    .fetchOne(0, Integer.class),
                "pageTitle", sched.name()
                )
            );
    }
}
