/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.federal.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.media.ThymeleafMedia;
import com.eshabakhov.schoodule.page.PageRequest;
import java.util.List;
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
 * Controller for HTML responses for {@link FederalCurriculum}.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class FederalCurriculumHtmlController {

    /**
     * JOOQ Table for FederalCurriculum.
     */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    public FederalCurriculumHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView list(
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit,
        @RequestParam(name = "title", required = false) final String title
    ) throws Exception {
        Condition condition = DSL.trueCondition();
        if (title != null && !title.isBlank()) {
            condition = condition.and(
                FederalCurriculumHtmlController.CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", title)
                )
            );
        }
        final PageableList<FederalCurriculum> result = new FcsPostgres(this.ctx)
            .curriculums(condition, new PageRequest(limit, offset));
        return new ModelAndView("federal-curriculums/list")
            .addAllObjects(
                Map.of(
                    "pageTitle", "Федеральные учебные планы",
                    "curriculums", result.list().stream()
                        .map(fc -> ((ThymeleafMedia) fc.print(new ThymeleafMedia())).map())
                        .toList(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) result.total() / limit),
                    "hasNext", result.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView fragment(
        @RequestParam(name = "title", required = false) final String title,
        @RequestParam(name = "offset", defaultValue = "1") final int offset,
        @RequestParam(name = "limit", defaultValue = "15") final int limit
    ) throws Exception {
        Condition condition = DSL.trueCondition();
        if (title != null && !title.isBlank()) {
            condition = condition.and(
                FederalCurriculumHtmlController.CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", title)
                )
            );
        }
        final PageableList<FederalCurriculum> result = new FcsPostgres(this.ctx)
            .curriculums(condition, new PageRequest(limit, offset));
        final List<Map<String, Object>> curriculums = result.list().stream()
            .map(fc -> ((ThymeleafMedia) fc.print(new ThymeleafMedia())).map())
            .toList();
        return new ModelAndView("federal-curriculums/list :: curriculums-grid")
            .addAllObjects(
                Map.of(
                    "curriculums", curriculums,
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) result.total() / limit),
                    "hasNext", result.total() > (long) offset * limit,
                    "hasPrev", offset > 1
                )
            );
    }

    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    public static ModelAndView createForm() {
        return new ModelAndView("federal-curriculums/create")
            .addAllObjects(
                Map.of(
                    "pageTitle", "Новый федеральный учебный план",
                    "levels", FederalCurriculum.Level.values(),
                    "studyWeeks", FederalCurriculum.Week.values()
                )
            );
    }

    @PostMapping("/create")
    public String create(
        @RequestParam final String title,
        @RequestParam(name = "level") final FederalCurriculum.Level level,
        @RequestParam(name = "week") final FederalCurriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        final String desc;
        if (description == null) {
            desc = null;
        } else {
            desc = description.trim();
        }
        return String.format(
            "redirect:/federal/curriculums/%d",
            new FcsPostgres(this.ctx)
                .create(
                    title.trim(), level, week, version.trim(), year.trim(), desc
                ).uid()
        );
    }

    @GetMapping(value = "/{curriculum}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView details(@PathVariable final long curriculum) throws Exception {
        final FederalCurriculum found = new FcsPostgres(this.ctx).curriculum(curriculum);
        final Map<String, Object> data = ((ThymeleafMedia) found.print(new ThymeleafMedia())).map();
        return new ModelAndView("federal-curriculums/details")
            .addAllObjects(data)
            .addAllObjects(
                Map.of(
                    "pageTitle", data.getOrDefault("title", ""),
                    "requirements", found.requirements()
                        .requirements(DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1))
                        .list().stream()
                        .map(req -> ((ThymeleafMedia) req.print(new ThymeleafMedia())).map())
                        .toList(),
                    "partTypes", FederalCurriculumRequirement.PartType.values()
                )
            );
    }

    @GetMapping(value = "/{curriculum}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView editForm(@PathVariable final long curriculum) throws Exception {
        return new ModelAndView("federal-curriculums/edit")
            .addAllObjects(
                (
                    (ThymeleafMedia) new FcsPostgres(this.ctx)
                        .curriculum(curriculum)
                        .print(new ThymeleafMedia())
                ).map()
            )
            .addAllObjects(
                Map.of(
                    "pageTitle", "Редактировать федеральный учебный план",
                    "levels", FederalCurriculum.Level.values(),
                    "studyWeeks", FederalCurriculum.Week.values()
                )
            );
    }

    @PostMapping("/{curriculum}/edit")
    public String edit(
        @PathVariable final long curriculum,
        @RequestParam final String title,
        @RequestParam(name = "level") final FederalCurriculum.Level level,
        @RequestParam(name = "week") final FederalCurriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        final String desc;
        if (description == null) {
            desc = null;
        } else {
            desc = description.trim();
        }
        new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .retitled(title.trim())
            .releveled(level)
            .reweeked(week)
            .reversioned(version.trim())
            .reyeared(year.trim())
            .redescriptioned(desc);
        return String.format("redirect:/federal/curriculums/%d", curriculum);
    }
}
