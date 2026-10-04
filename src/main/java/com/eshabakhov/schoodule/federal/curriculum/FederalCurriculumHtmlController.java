/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.media.ThymeleafMedia;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums")
@PreAuthorize("hasRole('ADMIN')")
public class FederalCurriculumHtmlController {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * New controller.
     *
     * @param ctx JOOQ context
     * @since 0.0.1
     */
    public FederalCurriculumHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Shows federal curriculums.
     *
     * @param page Pagination
     * @param sort Sorting
     * @param filters Filtering
     * @return Curriculums view
     * @throws Exception If selection fails
     * @since 0.0.1
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView list(
        final Page page,
        final Sorts sort,
        final Filters filters
    ) throws Exception {
        return new FcsPostgres(this.ctx)
            .selection(filters, page, sort)
            .print(new ThymeleafMedia("federal-curriculums/list", ""))
            .view();
    }

    /**
     * Shows the federal curriculums fragment.
     *
     * @param filters Filtering
     * @param page Pagination
     * @param sort Sorting
     * @return Curriculums fragment
     * @throws Exception If selection fails
     * @since 0.0.1
     */
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView fragment(
        final Filters filters,
        final Page page,
        final Sorts sort
    ) throws Exception {
        return new FcsPostgres(this.ctx)
            .selection(filters, page, sort).print(
                new ThymeleafMedia(
                    "federal-curriculums/list :: curriculums-grid",
                    ""
                )
            )
            .view();
    }

    /**
     * Shows the curriculum creation form.
     *
     * @return Creation form
     * @since 0.0.1
     */
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static ModelAndView createForm() {
        return new ThymeleafMedia("federal-curriculums/create", "").attributes(
            Map.of(
                "pageTitle", "Новый федеральный учебный план",
                "levels", FederalCurriculum.Level.values(),
                "studyWeeks", FederalCurriculum.Week.values()
            )
        ).view();
    }

    /**
     * Creates a federal curriculum from form values.
     *
     * @param title Curriculum title
     * @param level Education level
     * @param week Study week
     * @param version Curriculum version
     * @param year Academic year
     * @param description Description
     * @return Redirect to curriculum
     * @throws Exception If creation fails
     * @since 0.0.1
     */
    @PostMapping("/create")
    public String create(
        @RequestParam final String title,
        @RequestParam(name = "level") final FederalCurriculum.Level level,
        @RequestParam(name = "week") final FederalCurriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        return String.format(
            "redirect:/federal/curriculums/%d",
            new FcsPostgres(this.ctx).create(
                title.trim(),
                level,
                week,
                version.trim(),
                year.trim(),
                Optional.ofNullable(description).map(String::trim).orElse(null)
            ).uid()
        );
    }

    /**
     * Shows curriculum details.
     *
     * @param curriculum Curriculum ID
     * @return Curriculum view
     * @throws Exception If lookup fails
     * @since 0.0.1
     */
    @GetMapping(value = "/{curriculum}", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView details(
        @PathVariable final long curriculum
    ) throws Exception {
        return new FcsPostgres(this.ctx)
            .curriculum(curriculum).print(
                new ThymeleafMedia(
                    "federal-curriculums/details",
                    "curriculum"
                )
            )
            .view();
    }

    /**
     * Shows curriculum requirements.
     *
     * @param curriculum Curriculum ID
     * @param filters Filtering
     * @param page Pagination
     * @param sort Sorting
     * @return Requirements view
     * @throws Exception If selection fails
     * @since 0.0.1
     */
    @GetMapping(value = "/{curriculum}/requirements", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView requirementsPage(
        @PathVariable final long curriculum,
        final Filters filters,
        final Page page,
        final Sorts sort
    ) throws Exception {
        final FederalCurriculum selected = new FcsPostgres(this.ctx)
            .curriculum(curriculum);
        return selected.requirements()
            .selection(filters, page, sort).print(
                selected.print(
                    new ThymeleafMedia(
                        "federal-curriculums/requirements",
                        ""
                    )
                )
            )
            .title("Требования: %s").attributes(
                FederalCurriculumHtmlController.requirementsModel(
                    curriculum,
                    sort
                )
            )
            .view();
    }

    /**
     * Shows the curriculum requirements fragment.
     *
     * @param curriculum Curriculum ID
     * @param filters Filtering
     * @param page Pagination
     * @param sort Sorting
     * @return Requirements fragment
     * @throws Exception If selection fails
     * @since 0.0.1
     */
    @GetMapping(value = "/{curriculum}/requirements/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView requirements(
        @PathVariable
        final long curriculum,
        final Filters filters,
        final Page page,
        final Sorts sort
    ) throws Exception {
        return new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .selection(filters, page, sort).print(
                new ThymeleafMedia(
                    "federal-curriculums/requirements :: requirements-results",
                    ""
                )
            ).attributes(
                FederalCurriculumHtmlController.requirementsModel(
                    curriculum,
                    sort
                )
            )
            .view();
    }

    /**
     * Shows the curriculum editing form.
     *
     * @param curriculum Curriculum ID
     * @return Editing form
     * @throws Exception If lookup fails
     * @since 0.0.1
     */
    @GetMapping(value = "/{curriculum}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView editForm(@PathVariable final long curriculum) throws Exception {
        return new FcsPostgres(this.ctx)
            .curriculum(curriculum).print(
                new ThymeleafMedia(
                    "federal-curriculums/edit",
                    ""
                )
            ).attributes(
                Map.of(
                    "pageTitle", "Редактировать федеральный учебный план",
                    "levels", FederalCurriculum.Level.values(),
                    "studyWeeks", FederalCurriculum.Week.values()
                )
            )
            .view();
    }

    /**
     * Updates a federal curriculum from form values.
     *
     * @param curriculum Curriculum ID
     * @param title Curriculum title
     * @param level Education level
     * @param week Study week
     * @param version Curriculum version
     * @param year Academic year
     * @param description Description
     * @return Redirect to curriculum
     * @throws Exception If update fails
     * @since 0.0.1
     */
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
        new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .retitled(title.trim())
            .releveled(level)
            .reweeked(week)
            .reversioned(version.trim())
            .reyeared(year.trim())
            .redescriptioned(Optional.ofNullable(description).map(String::trim).orElse(null));
        return String.format("redirect:/federal/curriculums/%d", curriculum);
    }

    private static Map<String, Object> requirementsModel(
        final long curriculum,
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
            Map.entry("partTypes", FederalCurriculumRequirement.PartType.values()),
            Map.entry("gradeSort", grade),
            Map.entry("subjectSort", subject),
            Map.entry("hoursSort", hours),
            Map.entry("partSort", part)
        );
    }
}
