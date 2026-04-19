/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.SlsPostgres;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for Html response {@link Schedule}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules")
public class SchedulesHtmlController {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    public SchedulesHtmlController(final DSLContext datasource) {
        this.datasource = datasource;
    }

    //@checkstyle ParameterNumberCheck (3 lines)
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String list(
        @PathVariable final long school,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "name", required = false) final String name,
        final Model model
    ) throws Exception {
        var condition = SchedulesHtmlController.SCHEDULE.IS_DELETED.eq(false);
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchedulesHtmlController.SCHEDULE.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final int limit = 10;
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Schedule> schedules = sch
            .schedules()
            .list(condition, new PageRequest(10, offset));
        model.addAttribute("school", sch);
        model.addAttribute("pageTitle", String.format("%s — расписания", sch.name()));
        model.addAttribute("schedules", schedules.list());
        model.addAttribute("page", offset);
        model.addAttribute("hasNext", schedules.total() > offset * limit);
        model.addAttribute("hasPrev", offset > 1);
        return "schedules/list";
    }
}
