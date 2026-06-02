/*
 * Р’В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.page.PageRequest;
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
 * Controller for Html response {@link FederalCurriculum}.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal-curriculums")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public class FederalCurriculumsHtmlController {

    /** JOOQ Table for FederalCurriculum. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    public FederalCurriculumsHtmlController(final DSLContext ctx) {
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
                FederalCurriculumsHtmlController.CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", title)
                )
            );
        }
        final PageableList<FederalCurriculum> curriculums = new FcsPostgres(this.ctx)
            .curriculums(condition, new PageRequest(limit, offset));
        return new ModelAndView("federal-curriculums/list")
            .addAllObjects(
                Map.of(
                    "pageTitle", "Федеральные учебные планы",
                    "curriculums", curriculums.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) curriculums.total() / limit),
                    "hasNext", curriculums.total() > (long) offset * limit,
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
                FederalCurriculumsHtmlController.CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", title)
                )
            );
        }
        final PageableList<FederalCurriculum> curriculums = new FcsPostgres(this.ctx)
            .curriculums(condition, new PageRequest(limit, offset));
        return new ModelAndView("federal-curriculums/list :: curriculums-grid")
            .addAllObjects(
                Map.of(
                    "curriculums", curriculums.list(),
                    "page", offset,
                    "limit", limit,
                    "totalPages", (int) Math.ceil((double) curriculums.total() / limit),
                    "hasNext", curriculums.total() > (long) offset * limit,
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
            "redirect:/federal-curriculums/%d",
            new FcsPostgres(this.ctx)
                .create(
                    title.trim(),
                    level,
                    week,
                    version.trim(),
                    year.trim(),
                    desc
                ).uid()
        );
    }

    @GetMapping(value = "/{curriculum}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView details(@PathVariable final long curriculum) throws Exception {
        final FederalCurriculum found = new FcsPostgres(this.ctx).curriculum(curriculum);
        return new ModelAndView("federal-curriculums/details")
            .addAllObjects(
                Map.of(
                    "pageTitle", found.title(),
                    "curriculum", found,
                    "requirements", found.requirements()
                        .requirements(
                            DSL.trueCondition(),
                            new PageRequest(Integer.MAX_VALUE, 1)
                        ).list(),
                    "partTypes", FederalCurriculumRequirement.PartType.values()
                )
            );
    }

    @GetMapping(value = "/{curriculum}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView editForm(@PathVariable final long curriculum) throws Exception {
        return new ModelAndView("federal-curriculums/edit")
            .addAllObjects(
                Map.of(
                    "pageTitle", "Редактировать федеральный учебный план",
                    "curriculum",  new FcsPostgres(this.ctx).curriculum(curriculum),
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
        return String.format("redirect:/federal-curriculums/%d", curriculum);
    }

    @PostMapping("/{curriculum}/requirements/create")
    public String createRequirement(
        @PathVariable final long curriculum,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final FederalCurriculumRequirement.PartType part
    ) throws Exception {
        new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .create(grade, subject.trim(), hours, part);
        return String.format("redirect:/federal-curriculums/%d", curriculum);
    }

    @PostMapping("/{curriculum}/requirements/{requirement}/edit")
    public String editRequirement(
        @PathVariable final long curriculum,
        @PathVariable final long requirement,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final FederalCurriculumRequirement.PartType part
    ) throws Exception {
        new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirement(requirement)
            .regraded(grade)
            .resubjected(subject.trim())
            .reweekled(hours)
            .reparted(part);
        return String.format("redirect:/federal-curriculums/%d", curriculum);
    }
}
