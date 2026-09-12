/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.federal.curriculum.filter.FlCdFcByTitle;
import com.eshabakhov.schoodule.federal.curriculum.requirement.filter.FlCdFcrByGrade;
import com.eshabakhov.schoodule.federal.curriculum.requirement.filter.FlCdFcrByPart;
import com.eshabakhov.schoodule.federal.curriculum.requirement.filter.FlCdFcrBySubject;
import com.eshabakhov.schoodule.federal.curriculum.requirement.sort.FcrStsJooq;
import com.eshabakhov.schoodule.federal.curriculum.sort.FcStsJooq;
import com.eshabakhov.schoodule.filter.FlCdTrue;
import com.eshabakhov.schoodule.filter.FlConditional;
import com.eshabakhov.schoodule.media.ThymeleafMedia;
import java.util.List;
import java.util.Locale;
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
@SuppressWarnings(
    {
        "PMD.AvoidDuplicateLiterals",
        "PMD.TooManyMethods",
        "PMD.UseObjectForClearerAPI"
    }
)
public class FederalCurriculumHtmlController {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    public FederalCurriculumHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView list(
        final Page page,
        final Sorts sort,
        @RequestParam(name = "title", required = false) final String title
    ) throws Exception {
        final PageableList<FederalCurriculum> result = new FcsPostgres(this.ctx)
            .curriculums(
                new FlCdFcByTitle(new FlCdTrue(), title),
                page,
                new FcStsJooq(sort)
            );
        return new ModelAndView("federal-curriculums/list")
            .addAllObjects(
                Map.of(
                    "pageTitle", "Федеральные учебные планы",
                    "curriculums", result.list().stream()
                        .map(fc -> ((ThymeleafMedia) fc.print(new ThymeleafMedia())).map())
                        .toList(),
                    "page", page.offset(),
                    "limit", page.limit(),
                    "totalPages", (int) Math.ceil((double) result.total() / page.limit()),
                    "hasNext", result.total() > (long) page.offset() * page.limit(),
                    "hasPrev", page.offset() > 1
                )
            );
    }

    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView fragment(
        @RequestParam(name = "title", required = false) final String title,
        final Page page,
        final Sorts sort
    ) throws Exception {
        final FlConditional filter = new FlCdFcByTitle(new FlCdTrue(), title);
        final PageableList<FederalCurriculum> result = new FcsPostgres(this.ctx)
            .curriculums(filter, page, new FcStsJooq(sort));
        final List<Map<String, Object>> curriculums = result.list().stream()
            .map(fc -> ((ThymeleafMedia) fc.print(new ThymeleafMedia())).map())
            .toList();
        return new ModelAndView("federal-curriculums/list :: curriculums-grid")
            .addAllObjects(
                Map.of(
                    "curriculums", curriculums,
                    "page", page.offset(),
                    "limit", page.limit(),
                    "totalPages", (int) Math.ceil((double) result.total() / page.limit()),
                    "hasNext", result.total() > (long) page.offset() * page.limit(),
                    "hasPrev", page.offset() > 1
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
    public ModelAndView details(
        @PathVariable final long curriculum
    ) throws Exception {
        final FederalCurriculum found = new FcsPostgres(this.ctx).curriculum(curriculum);
        final Map<String, Object> data = ((ThymeleafMedia) found.print(new ThymeleafMedia())).map();
        return new ModelAndView("federal-curriculums/details")
            .addAllObjects(data)
            .addObject("pageTitle", data.getOrDefault("title", ""));
    }

    @GetMapping(value = "/{curriculum}/requirements", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView requirementsPage(
        @PathVariable final long curriculum,
        @RequestParam(name = "grade", required = false)
        final Integer grade,
        @RequestParam(name = "subject", required = false)
        final String subject,
        @RequestParam(name = "part", required = false)
        final FederalCurriculumRequirement.PartType part,
        final Page page,
        final Sorts sort
    ) throws Exception {
        final FederalCurriculum found = new FcsPostgres(this.ctx).curriculum(curriculum);
        final Map<String, Object> data = ((ThymeleafMedia) found.print(new ThymeleafMedia())).map();
        final PageableList<FederalCurriculumRequirement> result = this.requirements(
            curriculum,
            new FlCdFcrByPart(
                new FlCdFcrBySubject(
                    new FlCdFcrByGrade(new FlCdTrue(), grade),
                    subject
                ),
                part
            ),
            page,
            sort
        );
        return new ModelAndView("federal-curriculums/requirements")
            .addAllObjects(data)
            .addAllObjects(
                FederalCurriculumHtmlController.requirementsModel(
                    curriculum, result, page, sort
                )
            )
            .addObject(
                "pageTitle",
                String.format("Требования: %s", data.getOrDefault("title", ""))
            );
    }

    @GetMapping(value = "/{curriculum}/requirements/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView requirements(
        @PathVariable
        final long curriculum,
        @RequestParam(name = "grade", required = false)
        final Integer grade,
        @RequestParam(name = "subject", required = false)
        final String subject,
        @RequestParam(name = "part", required = false)
        final FederalCurriculumRequirement.PartType part,
        final Page page,
        final Sorts sort
    ) throws Exception {
        final PageableList<FederalCurriculumRequirement> result = this.requirements(
            curriculum,
            new FlCdFcrByPart(
                new FlCdFcrBySubject(
                    new FlCdFcrByGrade(new FlCdTrue(), grade),
                    subject
                ),
                part
            ),
            page,
            sort
        );
        return new ModelAndView("federal-curriculums/requirements :: requirements-results")
            .addAllObjects(
                FederalCurriculumHtmlController.requirementsModel(
                    curriculum, result, page, sort
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

    /**
     * Builds requirements list model with pagination metadata.
     *
     * @param curriculum Curriculum ID
     * @param result Requirements page
     * @param page Page request
     * @param sort Sorting parameters
     * @return Model attributes
     */
    private static Map<String, Object> requirementsModel(
        final long curriculum,
        final PageableList<FederalCurriculumRequirement> result,
        final Page page,
        final Sorts sort
    ) {
        String grade = "";
        String subject = "";
        String hours = "";
        String part = "";
        for (final Sort item : sort.sorts()) {
            final String direction;
            if (Sort.Direction.NONE.equals(item.direction())) {
                direction = "";
            } else {
                direction = item.direction().name().toLowerCase(Locale.ROOT);
            }
            if ("grade".equals(item.name())) {
                grade = direction;
            } else if ("subject".equals(item.name())) {
                subject = direction;
            } else if ("hours".equals(item.name())) {
                hours = direction;
            } else if ("part".equals(item.name())) {
                part = direction;
            }
        }
        return Map.ofEntries(
            Map.entry("id", curriculum),
            Map.entry(
                "requirements",
                result.list().stream()
                    .map(req -> ((ThymeleafMedia) req.print(new ThymeleafMedia())).map())
                    .toList()
            ),
            Map.entry("partTypes", FederalCurriculumRequirement.PartType.values()),
            Map.entry("page", page.offset()),
            Map.entry("limit", page.limit()),
            Map.entry("totalPages", (int) Math.ceil((double) result.total() / page.limit())),
            Map.entry("hasNext", result.total() > (long) page.offset() * page.limit()),
            Map.entry("hasPrev", page.offset() > 1),
            Map.entry("gradeSort", grade),
            Map.entry("subjectSort", subject),
            Map.entry("hoursSort", hours),
            Map.entry("partSort", part)
        );
    }

    /**
     * Fetches requirements with optional sorting.
     *
     * @param curriculum Curriculum ID
     * @param filter Search filter
     * @param page Page request
     * @param sort Sorting parameters
     * @return Requirements page
     * @throws Exception if listing fails
     */
    private PageableList<FederalCurriculumRequirement> requirements(
        final long curriculum,
        final FlConditional filter,
        final Page page,
        final Sorts sort
    ) throws Exception {
        return new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirements(filter, page, new FcrStsJooq(sort));
    }
}
