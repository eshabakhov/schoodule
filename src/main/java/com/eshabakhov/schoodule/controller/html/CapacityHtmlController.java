/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.tables.LessonAssignment;
import com.eshabakhov.schoodule.tables.Teacher;
import com.eshabakhov.schoodule.tables.TeacherCapacity;
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
 * Teacher capacity HTML controller.
 *
 * <p>Server-side rendering for capacity management.</p>
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}/capacity")
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class CapacityHtmlController {

    /**
     * Database context.
     */
    private final DSLContext datasource;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public CapacityHtmlController(final DSLContext dsl) {
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
        final var capacities = this.datasource
            .select(
                TeacherCapacity.TEACHER_CAPACITY.ID,
                Teacher.TEACHER.NAME,
                TeacherCapacity.TEACHER_CAPACITY.MAX_HOURS_PER_WEEK,
                TeacherCapacity.TEACHER_CAPACITY.MIN_HOURS_PER_WEEK,
                LessonAssignment.LESSON_ASSIGNMENT.HOURS_PER_WEEK.sum()
            )
            .from(TeacherCapacity.TEACHER_CAPACITY)
            .join(Teacher.TEACHER)
            .on(
                TeacherCapacity.TEACHER_CAPACITY.TEACHER_ID.eq(
                    Teacher.TEACHER.ID
                )
            )
            .leftJoin(LessonAssignment.LESSON_ASSIGNMENT)
            .on(
                LessonAssignment.LESSON_ASSIGNMENT.TEACHER_ID.eq(
                    Teacher.TEACHER.ID
                )
            )
            .where(TeacherCapacity.TEACHER_CAPACITY.SCHEDULE_ID.eq(sched.uid()))
            .groupBy(
                TeacherCapacity.TEACHER_CAPACITY.ID,
                Teacher.TEACHER.NAME,
                TeacherCapacity.TEACHER_CAPACITY.MAX_HOURS_PER_WEEK,
                TeacherCapacity.TEACHER_CAPACITY.MIN_HOURS_PER_WEEK
            )
            .fetch();
        model.addAttribute("school", sch);
        model.addAttribute("schedule", sched);
        model.addAttribute("capacities", capacities);
        model.addAttribute(
            "teachers",
            sch.teachers().list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        return "planning/capacity";
    }

    //@checkstyle ParameterNumberCheck (3 lines)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam final long teacher,
        @RequestParam final int maxhours,
        @RequestParam(required = false) final Integer minhours
    ) {
        String response;
        try {
            this.datasource.insertInto(TeacherCapacity.TEACHER_CAPACITY)
                .set(TeacherCapacity.TEACHER_CAPACITY.SCHEDULE_ID, schedule)
                .set(TeacherCapacity.TEACHER_CAPACITY.TEACHER_ID, teacher)
                .set(
                    TeacherCapacity.TEACHER_CAPACITY.MAX_HOURS_PER_WEEK,
                    maxhours
                )
                .set(
                    TeacherCapacity.TEACHER_CAPACITY.MIN_HOURS_PER_WEEK,
                    minhours
                )
                .execute();
            response = String.format(
                "redirect:/schools/%d/schedules/%d/capacity",
                school,
                schedule
            );
            //@checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            response = String.format(
                "redirect:/schools/%d/schedules/%d/capacity?error=capacity_create_failed",
                school,
                schedule
            );
        }
        return response;
    }

    @PostMapping("/{capacity}/delete")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String delete(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long capacity
    ) {
        this.datasource.deleteFrom(TeacherCapacity.TEACHER_CAPACITY)
            .where(TeacherCapacity.TEACHER_CAPACITY.ID.eq(capacity))
            .execute();
        return String.format(
            "redirect:/schools/%d/schedules/%d/capacity",
            school,
            schedule
        );
    }
}
