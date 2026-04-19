/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
import com.eshabakhov.schoodule.tables.LessonAssignment;
import com.eshabakhov.schoodule.tables.LessonSlot;
import com.eshabakhov.schoodule.tables.SchoolClass;
import com.eshabakhov.schoodule.tables.Subject;
import com.eshabakhov.schoodule.tables.Teacher;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Lesson assignments HTML controller.
 *
 * <p>Server-side rendering for assignments management.</p>
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}/assignments")
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class AssignmentsHtmlController {

    /**
     * Database context.
     */
    private final DSLContext datasource;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public AssignmentsHtmlController(final DSLContext dsl) {
        this.datasource = dsl;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String list(
        @PathVariable final long school,
        @PathVariable final long schedule,
        final Model model
    ) throws Exception {
        final var sch = new SlsPostgres(this.datasource).find(school);
        final var sched = sch.schedules().find(schedule);
        final var assignments = this.datasource
            .select(
                LessonAssignment.LESSON_ASSIGNMENT.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                Teacher.TEACHER.NAME,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK,
                LessonSlot.LESSON_SLOT.ID.count()
            )
            .from(LessonAssignment.LESSON_ASSIGNMENT)
            .join(ClassCurriculum.CLASS_CURRICULUM)
            .on(
                LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID.eq(
                    ClassCurriculum.CLASS_CURRICULUM.ID
                )
            )
            .join(SchoolClass.SCHOOL_CLASS)
            .on(
                ClassCurriculum.CLASS_CURRICULUM.SCHOOL_CLASS_ID.eq(
                    SchoolClass.SCHOOL_CLASS.ID
                )
            )
            .join(Subject.SUBJECT)
            .on(
                ClassCurriculum.CLASS_CURRICULUM.SUBJECT_ID.eq(
                    Subject.SUBJECT.ID
                )
            )
            .join(Teacher.TEACHER)
            .on(
                LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID.eq(
                    Teacher.TEACHER.ID
                )
            )
            .leftJoin(LessonSlot.LESSON_SLOT)
            .on(
                LessonSlot.LESSON_SLOT.LESSON_ASSIGNMENT_ID.eq(
                    LessonAssignment.LESSON_ASSIGNMENT.ID
                )
            )
            .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
            .groupBy(
                LessonAssignment.LESSON_ASSIGNMENT.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                Teacher.TEACHER.NAME,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK
            )
            .fetch();
        final var curriculums = this.datasource
            .select(
                ClassCurriculum.CLASS_CURRICULUM.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                ClassCurriculum.CLASS_CURRICULUM.HOURS_PER_WEEK
            )
            .from(ClassCurriculum.CLASS_CURRICULUM)
            .join(SchoolClass.SCHOOL_CLASS)
            .on(
                ClassCurriculum.CLASS_CURRICULUM.SCHOOL_CLASS_ID.eq(
                    SchoolClass.SCHOOL_CLASS.ID
                )
            )
            .join(Subject.SUBJECT)
            .on(
                ClassCurriculum.CLASS_CURRICULUM.SUBJECT_ID.eq(
                    Subject.SUBJECT.ID
                )
            )
            .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
            .fetch();
        model.addAttribute("school", sch);
        model.addAttribute("schedule", sched);
        model.addAttribute("assignments", assignments);
        model.addAttribute("curriculums", curriculums);
        model.addAttribute(
            "teachers",
            sch.teachers().list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        return "planning/assignments";
    }

    //@checkstyle ParameterNumberCheck (3 lines)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam final long curriculum,
        @RequestParam final long teacher,
        @RequestParam final int hours
    ) {
        String response;
        try {
            this.datasource.insertInto(LessonAssignment.LESSON_ASSIGNMENT)
                .set(
                    LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID,
                    curriculum
                )
                .set(LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID, teacher)
                .set(
                    LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK,
                    hours
                )
                .execute();
            response = String.format(
                "redirect:/schools/%d/schedules/%d/assignments",
                school,
                schedule
            );
            //@checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            response = String.format(
                "redirect:/schools/%d/schedules/%d/assignments?error=assignment_create_failed",
                school,
                schedule
            );
        }
        return response;
    }

    @PostMapping("/{assignment}/delete")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String delete(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long assignment
    ) {
        this.datasource.deleteFrom(LessonAssignment.LESSON_ASSIGNMENT)
            .where(LessonAssignment.LESSON_ASSIGNMENT.ID.eq(assignment))
            .execute();
        return String.format(
            "redirect:/schools/%d/schedules/%d/assignments",
            school,
            schedule
        );
    }
}
