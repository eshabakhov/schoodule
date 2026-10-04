/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.Teacher;
import java.util.Map;
import org.jooq.Condition;
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
 */
@Controller
@RequestMapping("/schools/{school}/teachers")
public class TeachersHtmlController {

    /** JOOQ Table for Teacher. */
    private static final com.eshabakhov.schoodule.tables.Teacher TEACHER =
        com.eshabakhov.schoodule.tables.Teacher.TEACHER;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /**
     * New controller.
     *
     * @param ctx Database context
     */
    public TeachersHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Render teachers.
     *
     * @param school School identifier
     * @param offset Page number
     * @param limit Page size
     * @param name Name filter
     * @return Teachers view
     * @throws Exception When teachers cannot be loaded
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
        Condition condition = TeachersHtmlController.TEACHER.IS_DELETED.eq(false)
            .and(TeachersHtmlController.TEACHER.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                TeachersHtmlController.TEACHER.NAME.likeIgnoreCase(
                    String.format("%%%s%%", name)
                )
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Teacher> teachers = sch
            .teachers()
            .teachers(condition, new PageRequest(limit, offset));
        return new ModelAndView("teachers/list").addAllObjects(
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

    /**
     * Render teachers fragment.
     *
     * @param school School identifier
     * @param name Name filter
     * @param offset Page number
     * @param limit Page size
     * @return Teachers fragment
     * @throws Exception When teachers cannot be loaded
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
        Condition condition = TeachersHtmlController.TEACHER.IS_DELETED.eq(false)
            .and(TeachersHtmlController.TEACHER.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                TeachersHtmlController.TEACHER.NAME.likeIgnoreCase(
                    String.format("%%%s%%", name)
                )
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Teacher> teachers = sch
            .teachers()
            .teachers(condition, new PageRequest(limit, offset));
        return new ModelAndView("teachers/list :: teachers-grid").addAllObjects(
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

    /**
     * Render teacher details.
     *
     * @param school School identifier
     * @param teacher Teacher identifier
     * @return Teacher view
     * @throws Exception When the teacher cannot be loaded
     */
    @GetMapping(value = "/{teacher}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long teacher
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        return new ModelAndView("teachers/details").addAllObjects(
            Map.of(
                "school", sch,
                "teacher", sch.teachers().teacher(teacher),
                "pageTitle", sch.teachers().teacher(teacher).name()
            )
        );
    }

    /**
     * Render teacher creation form.
     *
     * @param school School identifier
     * @return Creation form
     * @throws Exception When the school cannot be loaded
     */
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school) throws Exception {
        return new ModelAndView("teachers/create").addAllObjects(
            Map.of(
                "school", new SlsPostgres(this.ctx).school(school),
                "pageTitle", "Новый учитель"
            )
        );
    }

    /**
     * Create a teacher.
     *
     * @param school School identifier
     * @param name Teacher name
     * @return Redirect location
     * @throws Exception When the teacher cannot be created
     */
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
                .school(school)
                .teachers()
                .create(name.trim());
        }
        return result;
    }

    /**
     * Render teacher editing form.
     *
     * @param school School identifier
     * @param teacher Teacher identifier
     * @return Editing form
     * @throws Exception When the teacher cannot be loaded
     */
    @GetMapping(value = "/{teacher}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long teacher
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        return new ModelAndView("teachers/edit").addAllObjects(
            Map.of(
                "school", sch,
                "teacher", sch.teachers().teacher(teacher),
                "pageTitle", "Редактировать учителя"
            )
        );
    }

    /**
     * Update a teacher.
     *
     * @param school School identifier
     * @param teacher Teacher identifier
     * @param name Teacher name
     * @return Redirect location
     * @throws Exception When the teacher cannot be updated
     */
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
                .school(school)
                .teachers()
                .teacher(teacher)
                .renamed(name.trim());
            result = String.format("redirect:/schools/%d/teachers/%d", school, teacher);
        }
        return result;
    }
}
