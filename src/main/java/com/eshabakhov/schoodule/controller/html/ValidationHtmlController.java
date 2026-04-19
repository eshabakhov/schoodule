/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
import com.eshabakhov.schoodule.tables.LessonAssignment;
import com.eshabakhov.schoodule.tables.LessonSlot;
import com.eshabakhov.schoodule.tables.SchoolClass;
import com.eshabakhov.schoodule.tables.Subject;
import com.eshabakhov.schoodule.tables.Teacher;
import com.eshabakhov.schoodule.tables.TeacherCapacity;
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
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Schedule validation HTML controller.
 *
 * <p>Server-side rendering for schedule validation.</p>
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}/validation")
public class ValidationHtmlController {

    /**
     * Database context.
     */
    private final DSLContext datasource;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public ValidationHtmlController(final DSLContext dsl) {
        this.datasource = dsl;
    }

    // @checkstyle MethodLengthCheck (200 lines)
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String validate(
        @PathVariable final long school,
        @PathVariable final long schedule,
        final Model model
    ) throws Exception {
        final var sch = new SlsPostgres(this.datasource).find(school);
        final var sched = sch.schedules().find(schedule);
        final List<Map<String, Object>> issues = new ArrayList<>(100);
        final var curricgaps = this.datasource
            .select(
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                ClassCurriculum.CLASS_CURRICULUM.HOURS_PER_WEEK,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK.sum()
            )
            .from(ClassCurriculum.CLASS_CURRICULUM)
            .leftJoin(LessonAssignment.LESSON_ASSIGNMENT)
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
            .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(sched.uid()))
            .groupBy(
                ClassCurriculum.CLASS_CURRICULUM.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                ClassCurriculum.CLASS_CURRICULUM.HOURS_PER_WEEK
            )
            .having(
                ClassCurriculum.CLASS_CURRICULUM.HOURS_PER_WEEK.ne(
                    DSL.coalesce(
                        LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK.sum(),
                        DSL.inline(0)
                    ).cast(Integer.class)
                )
            )
            .fetch();
        for (final var gap : curricgaps) {
            final Map<String, Object> issue = new HashMap<>();
            issue.put("type", "CURRICULUM_GAP");
            issue.put("className", gap.get(0));
            issue.put("subjectName", gap.get(1));
            issue.put("planned", gap.get(2));
            if (gap.get(3) != null) {
                issue.put("assigned", gap.get(3));
            } else {
                issue.put("assigned", 0);
            }
            issues.add(issue);
        }
        final var capacissues = this.datasource
            .select(
                Teacher.TEACHER.NAME,
                TeacherCapacity.TEACHER_CAPACITY.MAX_HOURS_PER_WEEK,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK.sum()
            )
            .from(TeacherCapacity.TEACHER_CAPACITY)
            .leftJoin(LessonAssignment.LESSON_ASSIGNMENT)
            .on(
                LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID.eq(
                    TeacherCapacity.TEACHER_CAPACITY.TEACHER_ID
                )
            )
            .join(Teacher.TEACHER)
            .on(
                TeacherCapacity.TEACHER_CAPACITY.TEACHER_ID.eq(
                    Teacher.TEACHER.ID
                )
            )
            .where(TeacherCapacity.TEACHER_CAPACITY.SCHEDULE_ID.eq(sched.uid()))
            .groupBy(
                TeacherCapacity.TEACHER_CAPACITY.ID,
                Teacher.TEACHER.NAME,
                TeacherCapacity.TEACHER_CAPACITY.MAX_HOURS_PER_WEEK
            )
            .having(
                DSL.coalesce(
                    LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK.sum(),
                    0
                ).gt(
                    TeacherCapacity.TEACHER_CAPACITY.MAX_HOURS_PER_WEEK
                )
            )
            .fetch();
        for (final var capacity : capacissues) {
            final Map<String, Object> issue = new HashMap<>();
            issue.put("type", "TEACHER_OVERLOAD");
            issue.put("teacherName", capacity.get(0));
            issue.put("limit", capacity.get(1));
            if (capacity.get(2) != null) {
                issue.put("assigned", capacity.get(2));
            } else {
                issue.put("assigned", 0);
            }
            issues.add(issue);
        }
        final var slotgaps = this.datasource
            .select(
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                Teacher.TEACHER.NAME,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK,
                LessonSlot.LESSON_SLOT.ID.count()
            )
            .from(LessonAssignment.LESSON_ASSIGNMENT)
            .leftJoin(LessonSlot.LESSON_SLOT)
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
            .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(sched.uid()))
            .groupBy(
                LessonAssignment.LESSON_ASSIGNMENT.ID,
                SchoolClass.SCHOOL_CLASS.NAME,
                Subject.SUBJECT.NAME,
                Teacher.TEACHER.NAME,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK
            )
            .having(
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK.ne(
                    LessonSlot.LESSON_SLOT.ID.count()
                )
            )
            .fetch();
        for (final var slotgap : slotgaps) {
            final Map<String, Object> issue = new HashMap<>();
            issue.put("type", "SLOT_GAP");
            issue.put("className", slotgap.get(0));
            issue.put("subjectName", slotgap.get(1));
            issue.put("teacherName", slotgap.get(2));
            issue.put("planned", slotgap.get(3));
            issue.put("filled", slotgap.get(4));
            issues.add(issue);
        }
        model.addAttribute("school", sch);
        model.addAttribute("schedule", sched);
        model.addAttribute("valid", issues.isEmpty());
        model.addAttribute("issues", issues);
        model.addAttribute("issueCount", issues.size());
        return "planning/validation";
    }
}
