/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
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
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules/{schedule}/curriculum")
@SuppressWarnings("PMD.AvoidCatchingGenericException")
public class CurriculumHtmlController {
    /**
     * Database context.
     */
    private final DSLContext datasource;

    /**
     * Constructor.
     *
     * @param dsl Database context
     */
    public CurriculumHtmlController(final DSLContext dsl) {
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
        final var curriculums = sched
            .curriculums()
            .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1));
        model.addAttribute("school", sch);
        model.addAttribute("schedule", sched);
        model.addAttribute("curriculums", curriculums);
        model.addAttribute(
            "classes",
            sch
                .schoolClasses()
                .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        model.addAttribute(
            "subjects",
            sch
                .subjects()
                .list(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
        );
        return  "planning/curriculum";
    }

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
            new SlsPostgres(this.datasource)
                .find(school)
                .schedules()
                .find(schedule)
                .curriculums()
                .add(
                    new ScPostgres(this.datasource, clazz),
                    new SbPostgres(this.datasource, subject),
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

    @PostMapping("/{curriculum}/delete")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String delete(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long curriculum
    ) throws Exception {
        new SlsPostgres(this.datasource)
            .find(school)
            .schedules()
            .find(schedule)
            .curriculums()
            .remove(curriculum);
        return String.format(
            "redirect:/schools/%d/schedules/%d/curriculum",
            school,
            schedule
        );
    }
}
