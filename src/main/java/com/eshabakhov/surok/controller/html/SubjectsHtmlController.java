/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.controller.html;

import com.eshabakhov.surok.PageableList;
import com.eshabakhov.surok.School;
import com.eshabakhov.surok.media.MdThymeleaf;
import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.school.SlWeb;
import com.eshabakhov.surok.school.SlsPostgres;
import com.eshabakhov.surok.school.Subject;
import com.eshabakhov.surok.sql.ClasspathSql;
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
 * Controller for Html response {@link Subject}.
 *
 * @since 0.0.1
 */
@Controller
@RequestMapping("/schools/{school}/subjects")
public class SubjectsHtmlController {

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /**
     * New controller.
     *
     * @param ctx Database context
     */
    public SubjectsHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Render subjects.
     *
     * @param school School identifier
     * @param offset Page number
     * @param limit Page size
     * @param name Name filter
     * @return Subjects view
     * @throws Exception When subjects cannot be loaded
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
        Condition condition = DSL.condition(
            new ClasspathSql("subject/conditions/by-is-deleted-and-school-id.sql").text(),
            false,
            school
        );
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                DSL.condition(
                    new ClasspathSql("subject/conditions/by-name.sql").text(),
                    String.format("%%%s%%", name)
                )
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Subject> subjects = sch
            .subjects()
            .subjects(condition, new PageRequest(limit, offset));
        return new ModelAndView("subjects/list").addAllObjects(
            Map.of(
                "school", new SlWeb(sch).print(new MdThymeleaf("", "")).print().getModel(),
                "pageTitle", String.format(
                    "%s - предметы", sch.print(new MdThymeleaf("", ""))
                    .print().getModel().get("name")
                ),
                "subjects", subjects.list(),
                "page", offset,
                "limit", limit,
                "totalPages", (int) Math.ceil((double) subjects.total() / limit),
                "hasNext", subjects.total() > (long) offset * limit,
                "hasPrev", offset > 1
            )
        );
    }

    /**
     * Render subjects fragment.
     *
     * @param school School identifier
     * @param name Name filter
     * @param offset Page number
     * @param limit Page size
     * @return Subjects fragment
     * @throws Exception When subjects cannot be loaded
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
        Condition condition = DSL.condition(
            new ClasspathSql("subject/conditions/by-is-deleted-and-school-id.sql").text(),
            false,
            school
        );
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                DSL.condition(
                    new ClasspathSql("subject/conditions/by-name.sql").text(),
                    String.format("%%%s%%", name)
                )
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Subject> subjects = sch
            .subjects()
            .subjects(condition, new PageRequest(limit, offset));
        return new ModelAndView("subjects/list :: subjects-grid").addAllObjects(
            Map.of(
                "school", new SlWeb(sch).print(new MdThymeleaf("", "")).print().getModel(),
                "subjects", subjects.list(),
                "page", offset,
                "limit", limit,
                "totalPages", (int) Math.ceil((double) subjects.total() / limit),
                "hasNext", subjects.total() > (long) offset * limit,
                "hasPrev", offset > 1
            )
        );
    }

    /**
     * Render subject details.
     *
     * @param school School identifier
     * @param subject Subject identifier
     * @return Subject view
     * @throws Exception When the subject cannot be loaded
     */
    @GetMapping(value = "/{subject}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long subject
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        final Subject sub = sch.subjects().subject(subject);
        return new ModelAndView("subjects/details").addAllObjects(
            Map.of(
                "school", new SlWeb(sch).print(new MdThymeleaf("", "")).print().getModel(),
                "subject", sub,
                "pageTitle", sub.name()
            )
        );
    }

    /**
     * Render subject creation form.
     *
     * @param school School identifier
     * @return Creation form
     * @throws Exception When the school cannot be loaded
     */
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school) throws Exception {
        return new ModelAndView("subjects/create").addAllObjects(
            Map.of(
                "school", new SlWeb(new SlsPostgres(this.ctx).school(school))
                .print(new MdThymeleaf("", "")).print().getModel(),
                "pageTitle", "Новый предмет"
            )
        );
    }

    /**
     * Create a subject.
     *
     * @param school School identifier
     * @param name Subject name
     * @return Redirect location
     * @throws Exception When the subject cannot be created
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(@PathVariable final long school, @RequestParam final String name)
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/subjects/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/subjects", school);
            new SlsPostgres(this.ctx)
                .school(school)
                .subjects()
                .create(name.trim());
        }
        return result;
    }

    /**
     * Render subject editing form.
     *
     * @param school School identifier
     * @param subject Subject identifier
     * @return Editing form
     * @throws Exception When the subject cannot be loaded
     */
    @GetMapping(value = "/{subject}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long subject
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        return new ModelAndView("subjects/edit").addAllObjects(
            Map.of(
                "school", new SlWeb(sch).print(new MdThymeleaf("", "")).print().getModel(),
                "subject", sch.subjects().subject(subject),
                "pageTitle", "Редактировать предмет"
            )
        );
    }

    /**
     * Update a subject.
     *
     * @param school School identifier
     * @param subject Subject identifier
     * @param name Subject name
     * @return Redirect location
     * @throws Exception When the subject cannot be updated
     */
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
            new SlsPostgres(this.ctx)
                .school(school)
                .subjects()
                .subject(subject)
                .renamed(name.trim());
            result = String.format("redirect:/schools/%d/subjects/%d", school, subject);
        }
        return result;
    }
}
