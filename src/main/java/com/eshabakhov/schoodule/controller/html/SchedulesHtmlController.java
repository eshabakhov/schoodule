/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.SlsPostgres;
import java.util.Map;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for Html response {@link Schedule}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/schedules")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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
    public ModelAndView list(
        @PathVariable final long school,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit,
        @RequestParam(name = "name", required = false) final String name
    ) throws Exception {
        var condition = SchedulesHtmlController.SCHEDULE.IS_DELETED.eq(false)
            .and(SchedulesHtmlController.SCHEDULE.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchedulesHtmlController.SCHEDULE.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Schedule> schedules = sch
            .schedules()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("schedules/list")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "pageTitle", String.format("%s — расписания", sch.name()),
                    "schedules", schedules.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) schedules.total() / limit),
                    "hasNext", schedules.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    //@checkstyle ParameterNumberCheck (3 lines)
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView fragment(
        @PathVariable final long school,
        @RequestParam(name = "name", required = false) final String name,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit
    ) throws Exception {
        var condition = SchedulesHtmlController.SCHEDULE.IS_DELETED.eq(false)
            .and(SchedulesHtmlController.SCHEDULE.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchedulesHtmlController.SCHEDULE.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Schedule> schedules = sch
            .schedules()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("schedules/list :: schedules-grid")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "schedules", schedules.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) schedules.total() / limit),
                    "hasNext", schedules.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school)
        throws Exception {
        return new ModelAndView("schedules/create")
            .addAllObjects(
                Map.of(
                    "school", new SlsPostgres(this.datasource).find(school),
                    "pageTitle", "Новое расписание"
                )
            );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(@PathVariable final long school, @RequestParam final String name)
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/schedules/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/schedules", school);
            new SlsPostgres(this.datasource)
                .find(school)
                .schedules()
                .add(name.trim());
        }
        return result;
    }

    @GetMapping(value = "/{schedule}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long schedule
    ) throws Exception {
        final School sch = new SlsPostgres(this.datasource).find(school);
        return new ModelAndView("schedules/edit")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "schedule", sch.schedules().find(schedule),
                    "pageTitle", "Редактировать расписание"
                )
            );
    }

    @PostMapping("/{schedule}/edit")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String edit(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam final String name
    ) throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format(
                "redirect:/schools/%d/schedules/%d/edit?error=empty", school, schedule
            );
        } else {
            new SlsPostgres(this.datasource)
                .find(school)
                .schedules()
                .put(schedule, name.trim());
            result = String.format("redirect:/schools/%d/schedules/%d", school, schedule);
        }
        return result;
    }
}
