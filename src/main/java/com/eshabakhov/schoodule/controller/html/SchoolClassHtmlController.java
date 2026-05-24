/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SchoolClass;
import com.eshabakhov.schoodule.school.SlsPostgres;
import java.util.Map;
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
 * Controller for Html response {@link SchoolClass}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/classes")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SchoolClassHtmlController {

    /** JOOQ Table for SchoolClass. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass SCHOOL_CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    public SchoolClassHtmlController(final DSLContext ctx) {
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
        var condition = SchoolClassHtmlController.SCHOOL_CLASS.IS_DELETED.eq(false)
            .and(SchoolClassHtmlController.SCHOOL_CLASS.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                DSL.concat(
                    SchoolClassHtmlController.SCHOOL_CLASS.GRADE.cast(String.class),
                    SchoolClassHtmlController.SCHOOL_CLASS.LITERA
                ).likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<SchoolClass> classes = sch
            .schoolClasses()
            .classes(condition, new PageRequest(limit, offset));
        return new ModelAndView("schoolclasses/list")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "pageTitle", String.format("%s — школьные классы", sch.name()),
                    "classes", classes.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) classes.total() / limit),
                    "hasNext", classes.total() > (long) offset * limit,
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
        var condition = SchoolClassHtmlController.SCHOOL_CLASS.IS_DELETED.eq(false)
            .and(SchoolClassHtmlController.SCHOOL_CLASS.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                DSL.concat(
                    SchoolClassHtmlController.SCHOOL_CLASS.GRADE.cast(String.class),
                    SchoolClassHtmlController.SCHOOL_CLASS.LITERA
                ).likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<SchoolClass> classes = sch
            .schoolClasses()
            .classes(condition, new PageRequest(limit, offset));
        return new ModelAndView("schoolclasses/list :: school-classes-grid")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "classes", classes.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) classes.total() / limit),
                    "hasNext", classes.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/{clazz}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long clazz
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        final SchoolClass scl = sch.schoolClasses().clazz(clazz);
        return new ModelAndView("schoolclasses/details")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "clazz", scl,
                    "pageTitle", scl.name()
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school)
        throws Exception {
        return new ModelAndView("schoolclasses/create")
            .addAllObjects(
                Map.of(
                    "school", new SlsPostgres(this.ctx).school(school),
                    "pageTitle", "Новый класс"
                )
            );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(
        @PathVariable final long school,
        @RequestParam final String litera,
        @RequestParam final Integer grade
    ) throws Exception {
        final String result;
        if (litera == null || litera.isBlank() || grade == null) {
            result = String.format("redirect:/schools/%d/classes/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/classes", school);
            new SlsPostgres(this.ctx)
                .school(school)
                .schoolClasses()
                .create(litera.trim(), grade);
        }
        return result;
    }

    @GetMapping(value = "/{clazz}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long clazz
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        return new ModelAndView("schoolclasses/edit")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "clazz", sch.schoolClasses().clazz(clazz),
                    "pageTitle", "Редактировать класс"
                )
            );
    }

    @PostMapping("/{clazz}/edit")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    //@checkstyle ParameterNumberCheck (1 line)
    public String edit(
        @PathVariable final long school,
        @PathVariable final long clazz,
        @RequestParam final String litera,
        @RequestParam final Integer grade
    ) throws Exception {
        final String result;
        if (litera == null || litera.isBlank() || grade == null) {
            result = String.format(
                "redirect:/schools/%d/classes/%d/edit?error=empty", school, clazz
            );
        } else {
            new SlsPostgres(this.ctx)
                .school(school)
                .schoolClasses()
                .clazz(school)
                .regraded(grade)
                .reliterated(litera.trim());
            result = String.format("redirect:/schools/%d/classes/%d", school, clazz);
        }
        return result;
    }
}
