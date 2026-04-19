/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.enums.DayType;
import org.jooq.DSLContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Lesson slots HTML controller.
 *
 * <p>Server-side rendering for slot placement.</p>
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{schoolid}/schedules/{scheduleid}/slots")
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class SlotsHtmlController {

    /** JOOQ School table. */
    private static final com.eshabakhov.schoodule.tables.School SCHOOL =
        com.eshabakhov.schoodule.tables.School.SCHOOL;

    /** JOOQ Schedule table. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** JOOQ Lesson assignment table. */
    private static final com.eshabakhov.schoodule.tables.LessonAssignment ASSIGNMENT =
        com.eshabakhov.schoodule.tables.LessonAssignment.LESSON_ASSIGNMENT;

    /** JOOQ Class curriculum table. */
    private static final com.eshabakhov.schoodule.tables.ClassCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.ClassCurriculum.CLASS_CURRICULUM;

    /** JOOQ School class table. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** JOOQ Subject table. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** JOOQ Teacher table. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** JOOQ Cabinet table. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /**
     * Database context.
     */
    private final DSLContext datasource;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public SlotsHtmlController(final DSLContext dsl) {
        this.datasource = dsl;
    }

    @GetMapping("/place")
    @PreAuthorize("hasRole('ADMIN') or #schoolid == authentication.principal.info().school()")
    //@checkstyle ParameterNumberCheck (2 lines)
    public String placeForm(
        @PathVariable final long schoolid,
        @PathVariable final long scheduleid,
        @RequestParam final long assignmentid,
        final Model model
    ) {
        final String response;
        final var school = this.datasource
            .selectFrom(SlotsHtmlController.SCHOOL)
            .where(SlotsHtmlController.SCHOOL.ID.eq(schoolid))
            .fetchOne();
        final var schedule = this.datasource
            .selectFrom(SlotsHtmlController.SCHEDULE)
            .where(SlotsHtmlController.SCHEDULE.ID.eq(scheduleid))
            .fetchOne();
        final var details = this.datasource
            .select(
                SlotsHtmlController.CLASS.NAME,
                SlotsHtmlController.SUBJECT.NAME,
                SlotsHtmlController.TEACHER.NAME
            )
            .from(SlotsHtmlController.ASSIGNMENT)
            .join(SlotsHtmlController.CURRICULUM)
            .on(
                SlotsHtmlController.ASSIGNMENT.CLASS_CURRICULUM_ID.eq(
                    SlotsHtmlController.CURRICULUM.ID
                )
            )
            .join(SlotsHtmlController.CLASS)
            .on(
                SlotsHtmlController.CURRICULUM.SCHOOL_CLASS_ID.eq(
                    SlotsHtmlController.CLASS.ID
                )
            )
            .join(SlotsHtmlController.SUBJECT)
            .on(
                SlotsHtmlController.CURRICULUM.SUBJECT_ID.eq(
                    SlotsHtmlController.SUBJECT.ID
                )
            )
            .join(SlotsHtmlController.TEACHER)
            .on(
                SlotsHtmlController.ASSIGNMENT.TEACHER_ID.eq(
                    SlotsHtmlController.TEACHER.ID
                )
            )
            .where(SlotsHtmlController.ASSIGNMENT.ID.eq(assignmentid))
            .fetchOne();
        if (school == null || schedule == null || details == null) {
            response = "error/404";
        } else {
            final var cabinets = this.datasource
                .selectFrom(SlotsHtmlController.CABINET)
                .where(SlotsHtmlController.CABINET.SCHOOL_ID.eq(schoolid))
                .fetch();
            model.addAttribute("schoolId", schoolid);
            model.addAttribute("schoolName", school.getName());
            model.addAttribute("scheduleId", scheduleid);
            model.addAttribute("scheduleName", schedule.getName());
            model.addAttribute("assignmentId", assignmentid);
            model.addAttribute("className", details.get(0));
            model.addAttribute("subjectName", details.get(1));
            model.addAttribute("teacherName", details.get(2));
            model.addAttribute("cabinets", cabinets);
            response = "planning/place-slot";
        }
        return response;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #schoolid == authentication.principal.info().school()")
    //@checkstyle ParameterNumberCheck (2 lines)
    public String create(
        @PathVariable final long schoolid,
        @PathVariable final long scheduleid,
        @RequestParam final long assignmentid,
        @RequestParam final String day,
        @RequestParam final int lesson,
        @RequestParam(required = false) final Long cabinetid
    ) {
        String response;
        try {
            this.datasource
                .insertInto(
                    com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT
                )
                .set(
                    com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT.LESSON_ASSIGNMENT_ID,
                    assignmentid
                )
                .set(
                    com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT.DAY_OF_WEEK,
                    DayType.MONDAY
                )
                .set(
                    com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT.LESSON_NUMBER,
                    lesson
                )
                .set(
                    com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT.CABINET_ID,
                    cabinetid
                )
                .execute();
            response = String.format(
                "redirect:/schools/%d/schedules/%d/assignments",
                schoolid,
                scheduleid
            );
            //@checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            response = String.format(
                "redirect:/schools/%d/schedules/%d/slots/place?assignmentId=%d&error=%s",
                schoolid,
                scheduleid,
                assignmentid,
                ex.getMessage()
            );
        }
        return response;
    }

    @PostMapping("/{slotid}/delete")
    @PreAuthorize("hasRole('ADMIN') or #schoolid == authentication.principal.info().school()")
    public String delete(
        @PathVariable final long schoolid,
        @PathVariable final long scheduleid,
        @PathVariable final long slotid
    ) {
        this.datasource
            .deleteFrom(
                com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT
            )
            .where(
                com.eshabakhov.schoodule.tables.LessonSlot.LESSON_SLOT.ID.eq(slotid)
            )
            .execute();
        return String.format(
            "redirect:/schools/%d/schedules/%d/grid",
            schoolid,
            scheduleid
        );
    }
}
