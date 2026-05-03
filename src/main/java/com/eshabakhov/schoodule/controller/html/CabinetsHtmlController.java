/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Cabinet;
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
 * Controller for Html response {@link Cabinet}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/cabinets")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CabinetsHtmlController {

    /** JOOQ Table for Cabinet. */
    private static final com.eshabakhov.schoodule.tables.Cabinet CABINET =
        com.eshabakhov.schoodule.tables.Cabinet.CABINET;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext datasource;

    public CabinetsHtmlController(final DSLContext datasource) {
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
        var condition = CabinetsHtmlController.CABINET.IS_DELETED.eq(false)
            .and(CabinetsHtmlController.CABINET.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                CabinetsHtmlController.CABINET.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Cabinet> cabinets = sch
            .cabinets()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("cabinets/list")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "pageTitle", String.format("%s — кабинеты", sch.name()),
                    "cabinets", cabinets.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) cabinets.total() / limit),
                    "hasNext", cabinets.total() > (long) offset * limit,
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
        var condition = CabinetsHtmlController.CABINET.IS_DELETED.eq(false)
            .and(CabinetsHtmlController.CABINET.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                CabinetsHtmlController.CABINET.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.datasource).find(school);
        final PageableList<Cabinet> cabinets = sch
            .cabinets()
            .list(condition, new PageRequest(limit, offset));
        return new ModelAndView("cabinets/list :: cabinets-grid")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "cabinets", cabinets.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) cabinets.total() / limit),
                    "hasNext", cabinets.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/{cabinet}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long cabinet
    ) throws Exception {
        final School sch = new SlsPostgres(this.datasource).find(school);
        final Cabinet cab = sch.cabinets().find(cabinet);
        return new ModelAndView("cabinets/details")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "cabinet", cab,
                    "pageTitle", cab.name()
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school)
        throws Exception {
        return new ModelAndView("cabinets/create")
            .addAllObjects(
                Map.of(
                    "school", new SlsPostgres(this.datasource).find(school),
                    "pageTitle", "Новый кабинет"
                )
            );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(@PathVariable final long school, @RequestParam final String name)
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/cabinets/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/cabinets", school);
            new SlsPostgres(this.datasource)
                .find(school)
                .cabinets()
                .add(name.trim());
        }
        return result;
    }

    @GetMapping(value = "/{cabinet}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long cabinet
    ) throws Exception {
        final School sch = new SlsPostgres(this.datasource).find(school);
        return new ModelAndView("cabinets/edit")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "cabinet", sch.cabinets().find(cabinet),
                    "pageTitle", "Редактировать кабинет"
                )
            );
    }

    @PostMapping("/{cabinet}/edit")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String edit(
        @PathVariable final long school,
        @PathVariable final long cabinet,
        @RequestParam final String name
    ) throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format(
                "redirect:/schools/%d/cabinets/%d/edit?error=empty", school, cabinet
            );
        } else {
            new SlsPostgres(this.datasource)
                .find(school)
                .cabinets()
                .put(cabinet, name.trim());
            result = String.format("redirect:/schools/%d/cabinets/%d", school, cabinet);
        }
        return result;
    }
}
