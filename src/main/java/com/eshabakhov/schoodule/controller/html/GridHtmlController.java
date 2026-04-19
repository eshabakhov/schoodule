/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.enums.DayType;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.tables.Cabinet;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
import com.eshabakhov.schoodule.tables.LessonAssignment;
import com.eshabakhov.schoodule.tables.LessonSlot;
import com.eshabakhov.schoodule.tables.SchoolClass;
import com.eshabakhov.schoodule.tables.Subject;
import com.eshabakhov.schoodule.tables.Teacher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Schedule grid HTML controller.
 *
 * <p>Server-side rendering for schedule grid.</p>
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}/grid")
public class GridHtmlController {

    /** Database context. */
    private final DSLContext ctx;

    public GridHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String grid(
        @PathVariable final long school,
        @PathVariable final long schedule,
        final Model model
    ) throws Exception {
        final var sch = new SlsPostgres(this.ctx).find(school);
        final var sched = sch.schedules().find(schedule);
        final var slots = this.ctx
            .select(
                LessonSlot.LESSON_SLOT.ID,
                LessonSlot.LESSON_SLOT.DAY_OF_WEEK,
                LessonSlot.LESSON_SLOT.LESSON_NUMBER,
                SchoolClass.SCHOOL_CLASS.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                Teacher.TEACHER.NAME,
                Cabinet.CABINET.NAME,
                LessonAssignment.LESSON_ASSIGNMENT.ID
            )
            .from(LessonSlot.LESSON_SLOT)
            .join(LessonAssignment.LESSON_ASSIGNMENT)
            .on(
                LessonSlot.LESSON_SLOT.LESSON_ASSIGNMENT_ID.eq(
                    LessonAssignment.LESSON_ASSIGNMENT.ID
                )
            )
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
            .leftJoin(Cabinet.CABINET)
            .on(LessonSlot.LESSON_SLOT.CABINET_ID.eq(Cabinet.CABINET.ID))
            .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(sched.uid()))
            .fetch();
        final var grid = new HashMap<String, Map<Integer, Map<Long, List<Map<String, Object>>>>>();
        for (final var slot : slots) {
            final var day = slot.get(LessonSlot.LESSON_SLOT.DAY_OF_WEEK);
            final var lesson = slot.get(LessonSlot.LESSON_SLOT.LESSON_NUMBER);
            final var classid = slot.get(SchoolClass.SCHOOL_CLASS.ID);
            grid.putIfAbsent(String.valueOf(day), new HashMap<>());
            grid.get(String.valueOf(day)).putIfAbsent(lesson, new HashMap<>());
            grid.get(String.valueOf(day)).get(lesson).putIfAbsent(classid, new ArrayList<>(5));
            final var cell = new HashMap<String, Object>();
            cell.put("slotId", slot.get(LessonSlot.LESSON_SLOT.ID));
            cell.put("subjectName", slot.get(5));
            cell.put("teacherName", slot.get(6));
            cell.put("cabinetName", slot.get(7));
            cell.put("assignmentId", slot.get(8));
            grid.get(String.valueOf(day)).get(lesson).get(classid).add(cell);
        }
        final var curriculums = this.ctx
            .select(
                ClassCurriculum.CLASS_CURRICULUM.ID,
                SchoolClass.SCHOOL_CLASS.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.ID,
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
            .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(sched.uid()))
            .fetch();
        final var curricview = curriculums.stream()
            .map(
                curriculum -> Map.of(
                    "id", curriculum.get(ClassCurriculum.CLASS_CURRICULUM.ID),
                    "classId", curriculum.get(SchoolClass.SCHOOL_CLASS.ID),
                    "className", curriculum.get(SchoolClass.SCHOOL_CLASS.NAME),
                    "subjectId", curriculum.get(Subject.SUBJECT.ID),
                    "subjectName", curriculum.get(Subject.SUBJECT.NAME),
                    "hoursPerWeek", curriculum.get(ClassCurriculum.CLASS_CURRICULUM.HOURS_PER_WEEK)
                )
            )
            .toList();
        model.addAttribute("school", sch);
        model.addAttribute("schedule", sched);
        model.addAttribute("grid", grid);
        model.addAttribute(
            "classes", sch
            .schoolClasses()
            .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        model.addAttribute("curriculums", curricview);
        model.addAttribute(
            "teachers", sch
            .teachers()
            .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        model.addAttribute(
            "cabinets", sch
            .cabinets()
            .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        return "planning/grid";
    }

    @PostMapping("/place")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    // @checkstyle ParameterNumberCheck (2 lines)
    // @checkstyle MethodLengthCheck (200 lines)
    public String placeFromGrid(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam final long curriculum,
        @RequestParam final long teacher,
        @RequestParam final String day,
        @RequestParam final int lesson,
        @RequestParam(required = false) final Long cabinet
    ) {
        final var existassign = this.ctx
            .selectFrom(LessonAssignment.LESSON_ASSIGNMENT)
            .where(
                LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID.eq(curriculum)
                    .and(LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID.eq(teacher))
            )
            .fetchOne();
        final long assignmentid;
        if (existassign != null) {
            assignmentid = existassign.getId();
        } else {
            final var curric = this.ctx
                .selectFrom(ClassCurriculum.CLASS_CURRICULUM)
                .where(ClassCurriculum.CLASS_CURRICULUM.ID.eq(curriculum))
                .fetchOne();
            if (curric == null) {
                throw new IllegalStateException("Curriculum not found");
            }
            final var newassign = this.ctx
                .insertInto(LessonAssignment.LESSON_ASSIGNMENT)
                .set(
                    LessonAssignment.LESSON_ASSIGNMENT.CLASS_CURRICULUM_ID,
                    curriculum
                )
                .set(LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID, teacher)
                .set(
                    LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK,
                    curric.getHoursPerWeek()
                )
                .returning()
                .fetchOne();
            if (newassign == null) {
                throw new IllegalStateException("Failed to create assignment");
            }
            assignmentid = newassign.getId();
        }
        this.ctx.insertInto(LessonSlot.LESSON_SLOT)
            .set(LessonSlot.LESSON_SLOT.LESSON_ASSIGNMENT_ID, assignmentid)
            .set(LessonSlot.LESSON_SLOT.DAY_OF_WEEK, DayType.MONDAY)
            .set(LessonSlot.LESSON_SLOT.LESSON_NUMBER, lesson)
            .set(LessonSlot.LESSON_SLOT.CABINET_ID, cabinet)
            .execute();
        return String.format(
            "redirect:/schools/%d/schedules/%d/grid",
            school,
            schedule
        );
    }
}
