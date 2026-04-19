/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SchoolClass;
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
 * Controller for Html response {@link SchoolClass}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/classes")
public class SchoolClassHtmlController {

    /** JOOQ Table for SchoolClass. */
    private static final com.eshabakhov.schoodule.tables.SchoolClass SCHOOL_CLASS =
        com.eshabakhov.schoodule.tables.SchoolClass.SCHOOL_CLASS;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    public SchoolClassHtmlController(final DSLContext datasource) {
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
        var condition = SchoolClassHtmlController.SCHOOL_CLASS.IS_DELETED.eq(false);
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchoolClassHtmlController.SCHOOL_CLASS.NAME
                    .likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final int limit = 10;
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<SchoolClass> classes = sch
            .schoolClasses()
            .list(condition, new PageRequest(limit, offset));
        model.addAttribute("school", sch);
        model.addAttribute("pageTitle", String.format("%s — школьные классы", sch.name()));
        model.addAttribute("classes", classes.list());
        model.addAttribute("page", offset);
        model.addAttribute("hasNext", classes.total() > offset * limit);
        model.addAttribute("hasPrev", offset > 1);
        return "schoolclasses/list";
    }
}
