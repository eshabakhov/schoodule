/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.schedule.ClassCurriculum;
import com.eshabakhov.schoodule.school.schoolclass.ScPostgres;
import com.eshabakhov.schoodule.school.subject.SbPostgres;
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
 * Class curriculum HTML controller.
 *
 * <p>Server-side rendering for curriculum management.</p>
 *
 * @since 0.0.1
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}/curriculum")
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class CurriculumHtmlController {

    /**
     * Database context.
     */
    private final DSLContext ctx;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public CurriculumHtmlController(final DSLContext dsl) {
        this.ctx = dsl;
    }

    /**
     * Render the curriculum list.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param model View model
     * @return Template name
     * @throws Exception When curriculum data cannot be loaded
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String list(
        @PathVariable final long school,
        @PathVariable final long schedule,
        final Model model
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        final Schedule sched = sch.schedules().schedule(schedule);
        final PageableList<ClassCurriculum> curriculums = sched
            .curriculums()
            .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1));
        model.addAttribute("school", sch);
        model.addAttribute("schedule", sched);
        model.addAttribute("curriculums", curriculums);
        model.addAttribute(
            "classes",
            sch
                .schoolClasses()
                .classes(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        model.addAttribute(
            "subjects",
            sch
                .subjects()
                .subjects(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        return "planning/curriculum";
    }

    /**
     * Create a curriculum entry.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param clazz Class identifier
     * @param subject Subject identifier
     * @param hours Weekly hours
     * @return Redirect location
     */
    @PostMapping("/create")
    //@checkstyle ParameterNumberCheck (2 lines)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam final long clazz,
        @RequestParam final long subject,
        @RequestParam final int hours
    ) {
        String response;
        try {
            new SlsPostgres(this.ctx)
                .school(school)
                .schedules()
                .schedule(schedule)
                .curriculums().create(
                    new ScPostgres(this.ctx, clazz),
                    new SbPostgres(this.ctx, subject),
                    hours
                );
            response = String.format(
                "redirect:/schools/%d/schedules/%d/curriculum",
                school,
                schedule
            );
            //@checkstyle IllegalCatch (1 line)
        } catch (final Exception ex) {
            response = String.format(
                "redirect:/schools/%d/schedules/%d/curriculum?error=%s",
                school,
                schedule,
                ex.getMessage()
            );
        }
        return response;
    }

    /**
     * Delete a curriculum entry.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param curriculum Curriculum identifier
     * @return Redirect location
     * @throws Exception When the entry cannot be deleted
     */
    @PostMapping("/{curriculum}/delete")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String delete(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long curriculum
    ) throws Exception {
        new SlsPostgres(this.ctx)
            .school(school)
            .schedules()
            .schedule(schedule)
            .curriculums()
            .remove(curriculum);
        return String.format(
            "redirect:/schools/%d/schedules/%d/curriculum",
            school,
            schedule
        );
    }
}
