/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.Subject;
import com.eshabakhov.schoodule.school.subject.SbBase;
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
 * Controller for Html response {@link Subject}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/subjects")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class SubjectsHtmlController {

    /** JOOQ Table for Subject. */
    private static final com.eshabakhov.schoodule.tables.Subject SUBJECT =
        com.eshabakhov.schoodule.tables.Subject.SUBJECT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    public SubjectsHtmlController(final DSLContext datasource) {
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
        var condition = SubjectsHtmlController.SUBJECT.IS_DELETED.eq(false)
            .and(SubjectsHtmlController.SUBJECT.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SubjectsHtmlController.SUBJECT.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Subject> subjects = sch
            .subjects()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("subjects/list")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "pageTitle", String.format("%s — предметы", sch.name()),
                    "subjects", subjects.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) subjects.total() / limit),
                    "hasNext", subjects.total() > (long) offset * limit,
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
        var condition = SubjectsHtmlController.SUBJECT.IS_DELETED.eq(false)
            .and(SubjectsHtmlController.SUBJECT.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SubjectsHtmlController.SUBJECT.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Subject> subjects = sch
            .subjects()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("subjects/list :: subjects-grid")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "subjects", subjects.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) subjects.total() / limit),
                    "hasNext", subjects.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/{subject}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long subject
    ) throws Exception {
        final School sch = new SlsPostgres(this.datasource).find(school);
        final Subject sub = sch.subjects().find(subject);
        return new ModelAndView("subjects/details")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "subject", sub,
                    "pageTitle", sub.name()
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school) throws Exception {
        return new ModelAndView("subjects/create")
            .addAllObjects(
                Map.of(
                    "school", new SlsPostgres(this.datasource).find(school),
                    "pageTitle", "Новый предмет"
                )
            );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(@PathVariable final long school, @RequestParam final String name)
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/subjects/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/subjects", school);
            new SlsPostgres(this.datasource)
                .find(school)
                .subjects()
                .create(new SbBase(name.trim()));
        }
        return result;
    }

    @GetMapping(value = "/{subject}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long subject
    ) throws Exception {
        final School sch = new SlsPostgres(this.datasource).find(school);
        return new ModelAndView("subjects/edit")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "subject", sch.subjects().find(subject),
                    "pageTitle", "Редактировать предмет"
                )
            );
    }

    @PostMapping("/{subject}/edit")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String edit(
        @PathVariable final long school,
        @PathVariable final long subject,
        @RequestParam final String name
    ) throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format(
                "redirect:/schools/%d/subjects/%d/edit?error=empty", school, subject
            );
        } else {
            new SlsPostgres(this.datasource)
                .find(school)
                .subjects()
                .put(new SbBase(subject, name.trim()));
            result = String.format("redirect:/schools/%d/subjects/%d", school, subject);
        }
        return result;
    }
}
