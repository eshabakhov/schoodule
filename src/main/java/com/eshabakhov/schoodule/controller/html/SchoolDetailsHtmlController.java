/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.school.SlsPostgres;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Html response {@link School} details.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}")
public class SchoolDetailsHtmlController {

    /** Database context. */
    private final DSLContext ctx;

    SchoolDetailsHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String details(
        @PathVariable
        final long school,
        final Model model
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).find(school);
        model.addAttribute("school", sch);
        model.addAttribute("pageTitle", sch.name());
        return "schools/details";
    }
}
