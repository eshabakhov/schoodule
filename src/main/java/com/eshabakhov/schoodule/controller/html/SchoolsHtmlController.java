/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.user.AuthUser;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for Html response {@link School}.
 *
 * @since 0.0.1
 */
@Controller
@RequestMapping("/schools")
public final class SchoolsHtmlController {

    /** JOOQ Table for School. */
    private static final com.eshabakhov.schoodule.tables.School SCHOOL =
        com.eshabakhov.schoodule.tables.School.SCHOOL;

    /** Admin required. */
    private static final String ADMIN_REQUIRED = "Admin required";

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    /**
     * New controller.
     *
     * @param ctx Database context
     */
    public SchoolsHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Render schools.
     *
     * @param offset Page number
     * @param limit Page size
     * @param name Name filter
     * @param user Authenticated user
     * @param model View model
     * @return Template name
     * @throws Exception When schools cannot be loaded
     */
    // @checkstyle ParameterNumberCheck (2 lines)
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String list(
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit,
        @RequestParam(name = "name", required = false) final String name,
        @AuthenticationPrincipal final AuthUser user,
        final Model model
    ) throws Exception {
        if (!user.isAdmin()) {
            throw new AccessDeniedException(SchoolsHtmlController.ADMIN_REQUIRED);
        }
        Condition condition = SchoolsHtmlController.SCHOOL.IS_DELETED.eq(false);
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchoolsHtmlController.SCHOOL.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final PageableList<School> schools = new SlsPostgres(this.ctx)
            .schools(condition, new PageRequest(limit, offset));
        model.addAttribute("pageTitle", "Школы");
        model.addAttribute("schools", schools.list());
        model.addAttribute("page", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("totalPages", (int) Math.ceil((double) schools.total() / limit));
        model.addAttribute("hasNext", schools.total() > (long) offset * limit);
        model.addAttribute("hasPrev", offset > 1);
        return "schools/list";
    }

    /**
     * Render schools fragment.
     *
     * @param name Name filter
     * @param offset Page number
     * @param limit Page size
     * @param user Authenticated user
     * @param model View model
     * @return Fragment name
     * @throws Exception When schools cannot be loaded
     */
    // @checkstyle ParameterNumberCheck (2 lines)
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public String fragment(
        @RequestParam(name = "name", required = false) final String name,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit,
        @AuthenticationPrincipal final AuthUser user,
        final Model model
    ) throws Exception {
        if (!user.isAdmin()) {
            throw new AccessDeniedException(SchoolsHtmlController.ADMIN_REQUIRED);
        }
        Condition condition = SchoolsHtmlController.SCHOOL.IS_DELETED.eq(false);
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                SchoolsHtmlController.SCHOOL.NAME.likeIgnoreCase(
                    String.format("%%%s%%", name)
                )
            );
        }
        final PageableList<School> schools =
            new SlsPostgres(this.ctx).schools(condition, new PageRequest(limit, offset));
        model.addAttribute("schools", schools.list());
        model.addAttribute("page", offset);
        model.addAttribute("limit", limit);
        model.addAttribute("totalPages", (int) Math.ceil((double) schools.total() / limit));
        model.addAttribute("hasNext", schools.total() > (long) offset * limit);
        model.addAttribute("hasPrev", offset > 1);
        return "schools/list :: schools-grid";
    }

    /**
     * Render school creation form.
     *
     * @param model View model
     * @param user Authenticated user
     * @return Template name
     */
    // @checkstyle NonStaticMethodCheck (2 lines)
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    public String createForm(final Model model, @AuthenticationPrincipal final AuthUser user) {
        if (!user.isAdmin()) {
            throw new AccessDeniedException(SchoolsHtmlController.ADMIN_REQUIRED);
        }
        model.addAttribute("pageTitle", "Новая школа");
        return "schools/create";
    }

    /**
     * Create a school.
     *
     * @param name School name
     * @param user Authenticated user
     * @return Redirect location
     * @throws Exception When the school cannot be created
     */
    @PostMapping("/create")
    public String create(
        @RequestParam
        final String name,
        @AuthenticationPrincipal
        final AuthUser user
    ) throws Exception {
        if (!user.isAdmin()) {
            throw new AccessDeniedException(SchoolsHtmlController.ADMIN_REQUIRED);
        }
        final String response;
        if (name == null || name.isBlank()) {
            response = "redirect:/schools/create?error=empty";
        } else {
            new SlsPostgres(this.ctx).create(name.trim());
            response = "redirect:/schools";
        }
        return response;
    }

    /**
     * Render school editing form.
     *
     * @param id School identifier
     * @param user Authenticated user
     * @param model View model
     * @return Template name
     * @throws Exception When the school cannot be loaded
     */
    @GetMapping(value = "/{id}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public String editForm(
        @PathVariable
        final long id,
        @AuthenticationPrincipal
        final AuthUser user,
        final Model model
    ) throws Exception {
        if (!user.isAdmin()) {
            throw new AccessDeniedException(SchoolsHtmlController.ADMIN_REQUIRED);
        }
        model.addAttribute("pageTitle", "Редактировать школу");
        model.addAttribute("school", new SlsPostgres(this.ctx).school(id));
        return "schools/edit";
    }

    /**
     * Update a school.
     *
     * @param id School identifier
     * @param name School name
     * @param user Authenticated user
     * @return Redirect location
     * @throws Exception When the school cannot be updated
     */
    @PostMapping("/{id}/edit")
    public String edit(
        @PathVariable
        final long id,
        @RequestParam
        final String name,
        @AuthenticationPrincipal
        final AuthUser user
    ) throws Exception {
        if (!user.isAdmin()) {
            throw new AccessDeniedException(SchoolsHtmlController.ADMIN_REQUIRED);
        }
        final String response;
        if (name == null || name.isBlank()) {
            response = String.format("redirect:/schools/%d/edit?error=empty", id);
        } else {
            new SlsPostgres(this.ctx).school(id).renamed(name.trim());
            response = "redirect:/schools";
        }
        return response;
    }
}
