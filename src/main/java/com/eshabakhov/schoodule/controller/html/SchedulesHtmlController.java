/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Schedule;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.tables.ClassCurriculum;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
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
 */
@Controller
@RequestMapping("/schools/{school}/schedules")
public class SchedulesHtmlController {

    /** JOOQ Table for Schedule. */
    private static final com.eshabakhov.schoodule.tables.Schedule SCHEDULE =
        com.eshabakhov.schoodule.tables.Schedule.SCHEDULE;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /**
     * New controller.
     *
     * @param ctx Database context
     */
    public SchedulesHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Render schedules.
     *
     * @param school School identifier
     * @param offset Page number
     * @param limit Page size
     * @param name Name filter
     * @return Schedules view
     * @throws Exception When schedules cannot be loaded
     */
    //@checkstyle ParameterNumberCheck (3 lines)
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView list(
        @PathVariable final long school,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit,
        @RequestParam(name = "name", required = false) final String name
    ) throws Exception {
        Condition condition = SchedulesHtmlController.SCHEDULE.IS_DELETED.eq(false)
            .and(SchedulesHtmlController.SCHEDULE.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchedulesHtmlController.SCHEDULE.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Schedule> schedules = sch
            .schedules()
            .schedules(condition, new PageRequest(limit, offset));
        return new ModelAndView("schedules/list").addAllObjects(
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

    /**
     * Render schedules fragment.
     *
     * @param school School identifier
     * @param name Name filter
     * @param offset Page number
     * @param limit Page size
     * @return Schedules fragment
     * @throws Exception When schedules cannot be loaded
     */
    //@checkstyle ParameterNumberCheck (3 lines)
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView fragment(
        @PathVariable final long school,
        @RequestParam(name = "name", required = false) final String name,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit
    ) throws Exception {
        Condition condition = SchedulesHtmlController.SCHEDULE.IS_DELETED.eq(false)
            .and(SchedulesHtmlController.SCHEDULE.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchedulesHtmlController.SCHEDULE.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Schedule> schedules = sch
            .schedules()
            .schedules(condition, new PageRequest(limit, offset));
        return new ModelAndView("schedules/list :: schedules-grid").addAllObjects(
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

    /**
     * Render schedule creation form.
     *
     * @param school School identifier
     * @return Creation form
     * @throws Exception When the school cannot be loaded
     */
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school) throws Exception {
        return new ModelAndView("schedules/create").addAllObjects(
            Map.of(
                "school", new SlsPostgres(this.ctx).school(school),
                "pageTitle", "Новое расписание"
            )
        );
    }

    /**
     * Create a schedule.
     *
     * @param school School identifier
     * @param name Schedule name
     * @return Redirect location
     * @throws Exception When the schedule cannot be created
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(@PathVariable final long school, @RequestParam final String name)
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/schedules/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/schedules", school);
            new SlsPostgres(this.ctx)
                .school(school)
                .schedules()
                .create(name.trim());
        }
        return result;
    }

    /**
     * Render schedule editing form.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @return Editing form
     * @throws Exception When the schedule cannot be loaded
     */
    @GetMapping(value = "/{schedule}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long schedule
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        return new ModelAndView("schedules/edit").addAllObjects(
            Map.of(
                "school", sch,
                "schedule", sch.schedules().schedule(schedule),
                "pageTitle", "Редактировать расписание"
            )
        );
    }

    /**
     * Update a schedule.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param name Schedule name
     * @return Redirect location
     * @throws Exception When the schedule cannot be updated
     */
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
            new SlsPostgres(this.ctx)
                .school(school)
                .schedules()
                .schedule(schedule)
                .renamed(name.trim());
            result = String.format("redirect:/schools/%d/schedules/%d", school, schedule);
        }
        return result;
    }

    /**
     * Render schedule details.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @return Schedule view
     * @throws Exception When the schedule cannot be loaded
     */
    @GetMapping(value = "/{schedule}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long schedule
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        final Schedule sched = sch.schedules().schedule(schedule);
        return new ModelAndView("schedules/details").addAllObjects(
            Map.of(
                "school", sch,
                "schedule", sched,
                "classCount", sch.schoolClasses()
                    .classes(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
                    .total(),
                "subjectCount", this.ctx
                    .select(DSL.countDistinct(ClassCurriculum.CLASS_CURRICULUM.SUBJECT_ID))
                    .from(ClassCurriculum.CLASS_CURRICULUM)
                    .where(ClassCurriculum.CLASS_CURRICULUM.SCHEDULE_ID.eq(schedule))
                    .fetchOne(0, Integer.class),
                "pageTitle", sched.name()
            )
        );
    }
}
