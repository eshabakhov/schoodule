/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.Teacher;
import com.eshabakhov.schoodule.school.teacher.ThBase;
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
 * Controller for Html response {@link Teacher}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/teachers")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class TeachersHtmlController {

    /** JOOQ Table for Teacher. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    public TeachersHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
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
        var condition = TeachersHtmlController.TEACHER.IS_DELETED.eq(false)
            .and(TeachersHtmlController.TEACHER.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                TeachersHtmlController.TEACHER.NAME.likeIgnoreCase(
                    String.format("%%%s%%", name)
                )
            );
        }
        final School sch = new SlsPostgres(this.ctx).find(school);
        final PageableList<Teacher> teachers = sch
            .teachers()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("teachers/list")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "pageTitle", String.format("%s — учителя", sch.name()),
                    "teachers", teachers.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) teachers.total() / limit),
                    "hasNext", teachers.total() > (long) offset * limit,
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
        var condition = TeachersHtmlController.TEACHER.IS_DELETED.eq(false)
            .and(TeachersHtmlController.TEACHER.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                TeachersHtmlController.TEACHER.NAME.likeIgnoreCase(
                    String.format("%%%s%%", name)
                )
            );
        }
        final School sch = new SlsPostgres(this.ctx).find(school);
        final PageableList<Teacher> teachers = sch
            .teachers()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("teachers/list :: teachers-grid")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "teachers", teachers.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) teachers.total() / limit),
                    "hasNext", teachers.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/{teacher}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long teacher
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).find(school);
        return new ModelAndView("teachers/details")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "teacher", sch.teachers().find(teacher),
                    "pageTitle", sch.teachers().find(teacher).name()
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school) throws Exception {
        return new ModelAndView("teachers/create")
            .addAllObjects(
                Map.of(
                    "school", new SlsPostgres(this.ctx).find(school),
                    "pageTitle", "Новый учитель"
                )
            );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(@PathVariable final long school, @RequestParam final String name)
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/teachers/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/teachers", school);
            new SlsPostgres(this.ctx)
                .find(school)
                .teachers()
                .create(new ThBase(name.trim()));
        }
        return result;
    }

    @GetMapping(value = "/{teacher}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long teacher
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).find(school);
        return new ModelAndView("teachers/edit")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "teacher", sch.teachers().find(teacher),
                    "pageTitle", "Редактировать учителя"
                )
            );
    }

    @PostMapping("/{teacher}/edit")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String edit(
        @PathVariable final long school,
        @PathVariable final long teacher,
        @RequestParam final String name
    ) throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format(
                "redirect:/schools/%d/teachers/%d/edit?error=empty", school, teacher
            );
        } else {
            new SlsPostgres(this.ctx)
                .find(school)
                .teachers()
                .put(new ThBase(teacher, name.trim()));
            result = String.format("redirect:/schools/%d/teachers/%d", school, teacher);
        }
        return result;
    }
}
