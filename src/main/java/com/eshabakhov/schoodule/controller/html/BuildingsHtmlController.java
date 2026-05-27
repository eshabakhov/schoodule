/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.School;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Building;
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
 * Controller for Html response {@link Building}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/schools/{school}/buildings/")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class BuildingsHtmlController {

    /** JOOQ Table for Building. */
    private static final com.eshabakhov.schoodule.tables.Building BUILDING =
        com.eshabakhov.schoodule.tables.Building.BUILDING;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    public BuildingsHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView list(
        @PathVariable final long school,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit,
        @RequestParam(name = "name", required = false) final String name
    ) throws Exception {
        var condition = BuildingsHtmlController.BUILDING.IS_DELETED.eq(false)
            .and(BuildingsHtmlController.BUILDING.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                BuildingsHtmlController.BUILDING.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Building> buildings = sch
            .buildings()
            .buildings(condition, new PageRequest(limit, offset));
        return new ModelAndView("buildings/list")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "pageTitle", String.format("%s — корпуса", sch.name()),
                    "buildings", buildings.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) buildings.total() / limit),
                    "hasNext", buildings.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView fragment(
        @PathVariable final long school,
        @RequestParam(name = "name", required = false) final String name,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit
    ) throws Exception {
        var condition = BuildingsHtmlController.BUILDING.IS_DELETED.eq(false)
            .and(BuildingsHtmlController.BUILDING.SCHOOL_ID.eq(school));
        if (name != null && !name.isBlank()) {
            condition = condition.and(
                BuildingsHtmlController.BUILDING.NAME.likeIgnoreCase(String.format("%%%s%%", name))
            );
        }
        final School sch = new SlsPostgres(this.ctx).school(school);
        final PageableList<Building> buildings = sch
            .buildings()
            .buildings(condition, new PageRequest(limit, offset));
        return new ModelAndView("buildings/list :: buildings-grid")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "buildings", buildings.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) buildings.total() / limit),
                    "hasNext", buildings.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/{building}", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView details(
        @PathVariable final long school,
        @PathVariable final long building
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        final Building build = sch.buildings().building(building);
        return new ModelAndView("buildings/details")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "building", build,
                    "pageTitle", build.name()
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView createForm(@PathVariable final long school)
        throws Exception {
        return new ModelAndView("buildings/create")
            .addAllObjects(
                Map.of(
                    "school", new SlsPostgres(this.ctx).school(school),
                    "pageTitle", "Новый кабинет"
                )
            );
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String create(
        @PathVariable final long school,
        @RequestParam final String name
    )
        throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format("redirect:/schools/%d/buildings/create?error=empty", school);
        } else {
            result = String.format("redirect:/schools/%d/buildings", school);
            new SlsPostgres(this.ctx)
                .school(school)
                .buildings()
                .create(name.trim());
        }
        return result;
    }

    @GetMapping(value = "/{building}/edit", produces = MediaType.TEXT_HTML_VALUE)
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public ModelAndView editForm(
        @PathVariable final long school,
        @PathVariable final long building
    ) throws Exception {
        final School sch = new SlsPostgres(this.ctx).school(school);
        return new ModelAndView("buildings/edit")
            .addAllObjects(
                Map.of(
                    "school", sch,
                    "building", sch.buildings().building(building),
                    "pageTitle", "Редактировать корпус"
                )
            );
    }

    @PostMapping("/{building}/edit")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    public String edit(
        @PathVariable final long school,
        @PathVariable final long building,
        @RequestParam final String name
    ) throws Exception {
        final String result;
        if (name == null || name.isBlank()) {
            result = String.format(
                "redirect:/schools/%d/buildings/%d/edit?error=empty", school, building
            );
        } else {
            new SlsPostgres(this.ctx)
                .school(school)
                .buildings()
                .building(building)
                .renamed(name.trim());
            result = String.format("redirect:/schools/%d/buildings/%d", school, building);
        }
        return result;
    }
}
