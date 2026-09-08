/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.enums.CurriculumPartType;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
import com.eshabakhov.schoodule.federal.curriculum.requirement.FcrPostgres;
import com.eshabakhov.schoodule.media.ThymeleafMedia;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.page.ResponsePageableList;
import java.util.List;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
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

    /** JOOQ Table for FederalCurriculumRequirement. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

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
        @RequestParam(name = "sortBy", required = false)
        final String sort,
        @RequestParam(name = "sortDir", required = false)
        final String direction,
        @RequestParam(name = "offset", defaultValue = "1")
        final int offset,
        @RequestParam(name = "limit", defaultValue = "15")
        final int limit
    ) throws Exception {
        final FederalCurriculum found = new FcsPostgres(this.ctx).curriculum(curriculum);
        final Map<String, Object> data = ((ThymeleafMedia) found.print(new ThymeleafMedia())).map();
        final PageableList<FederalCurriculumRequirement> result = this.requirements(
            curriculum,
            FederalCurriculumHtmlController.condition(grade, subject, part),
            new PageRequest(limit, offset),
            sort,
            direction
        );
        return new ModelAndView("federal-curriculums/requirements")
            .addAllObjects(data)
            .addAllObjects(
                FederalCurriculumHtmlController.requirementsModel(
                    curriculum, result, offset, limit, sort, direction
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
        @RequestParam(name = "sortBy", required = false)
        final String sort,
        @RequestParam(name = "sortDir", required = false)
        final String direction,
        @RequestParam(name = "offset", defaultValue = "1")
        final int offset,
        @RequestParam(name = "limit", defaultValue = "15")
        final int limit
    ) throws Exception {
        final PageableList<FederalCurriculumRequirement> result = this.requirements(
            curriculum,
            FederalCurriculumHtmlController.condition(grade, subject, part),
            new PageRequest(limit, offset),
            sort,
            direction
        );
        return new ModelAndView("federal-curriculums/requirements :: requirements-results")
            .addAllObjects(
                FederalCurriculumHtmlController.requirementsModel(
                    curriculum, result, offset, limit, sort, direction
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
     * @param offset Current page number
     * @param limit Page size
     * @param sort Sort field
     * @param direction Sort direction
     * @return Model attributes
     */
    private static Map<String, Object> requirementsModel(
        final long curriculum,
        final PageableList<FederalCurriculumRequirement> result,
        final int offset,
        final int limit,
        final String sort,
        final String direction
    ) {
        return Map.of(
            "id", curriculum,
            "requirements", result.list().stream()
                .map(req -> ((ThymeleafMedia) req.print(new ThymeleafMedia())).map())
                .toList(),
            "partTypes", FederalCurriculumRequirement.PartType.values(),
            "page", offset,
            "limit", limit,
            "totalPages", (int) Math.ceil((double) result.total() / limit),
            "hasNext", result.total() > (long) offset * limit,
            "hasPrev", offset > 1,
            "sortBy", FederalCurriculumHtmlController.sortName(sort),
            "sortDir", FederalCurriculumHtmlController.sortDirection(direction)
        );
    }

    /**
     * Fetches requirements with optional sorting.
     *
     * @param curriculum Curriculum ID
     * @param condition Search condition
     * @param page Page request
     * @param sort Sort field
     * @param direction Sort direction
     * @return Requirements page
     */
    private PageableList<FederalCurriculumRequirement> requirements(
        final long curriculum,
        final Condition condition,
        final PageRequest page,
        final String sort,
        final String direction
    ) {
        final Condition scoped = FederalCurriculumHtmlController.REQUIREMENT.FEDERAL_CURRICULUM_ID
            .eq(curriculum)
            .and(FederalCurriculumHtmlController.REQUIREMENT.IS_DELETED.eq(false))
            .and(condition);
        final SortField<?> order = FederalCurriculumHtmlController.sortField(sort, direction);
        final List<FederalCurriculumRequirement> list;
        if (order == null) {
            list = this.ctx
                .selectFrom(FederalCurriculumHtmlController.REQUIREMENT)
                .where(scoped)
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(selected -> new FcrPostgres(this.ctx, selected.getId()));
        } else {
            list = this.ctx
                .selectFrom(FederalCurriculumHtmlController.REQUIREMENT)
                .where(scoped)
                .orderBy(order)
                .limit(page.limit())
                .offset((page.offset() - 1) * page.limit())
                .fetch(selected -> new FcrPostgres(this.ctx, selected.getId()));
        }
        return new ResponsePageableList<>(
            list,
            this.ctx.fetchCount(
                this.ctx.selectFrom(FederalCurriculumHtmlController.REQUIREMENT).where(scoped)
            ),
            page
        );
    }

    /**
     * Builds sort field from request parameters.
     *
     * @param sort Sort field name
     * @param direction Sort direction
     * @return JOOQ sort field
     */
    private static SortField<?> sortField(final String sort, final String direction) {
        final Field<?> field = switch (FederalCurriculumHtmlController.sortName(sort)) {
            case "grade" -> FederalCurriculumHtmlController.REQUIREMENT.GRADE;
            case "subject" -> FederalCurriculumHtmlController.REQUIREMENT.SUBJECT_NAME;
            case "hours" -> FederalCurriculumHtmlController.REQUIREMENT.WEEKLY_HOURS;
            case "part" -> FederalCurriculumHtmlController.REQUIREMENT.PART_TYPE;
            default -> null;
        };
        final String dir = FederalCurriculumHtmlController.sortDirection(direction);
        final SortField<?> order;
        if (field == null || dir.isBlank()) {
            order = null;
        } else if ("asc".equals(dir)) {
            order = field.asc();
        } else {
            order = field.desc();
        }
        return order;
    }

    /**
     * Normalizes sort field name.
     *
     * @param sort Raw sort field name
     * @return Normalized sort field name
     */
    private static String sortName(final String sort) {
        final String name;
        if (
            "grade".equals(sort)
                || "subject".equals(sort)
                || "hours".equals(sort)
                || "part".equals(sort)
        ) {
            name = sort;
        } else {
            name = "";
        }
        return name;
    }

    /**
     * Normalizes sort direction.
     *
     * @param direction Raw sort direction
     * @return Normalized sort direction
     */
    private static String sortDirection(final String direction) {
        final String dir;
        if ("asc".equals(direction) || "desc".equals(direction)) {
            dir = direction;
        } else {
            dir = "";
        }
        return dir;
    }

    /**
     * Builds requirements search condition.
     *
     * @param grade Grade number
     * @param subject Subject name
     * @param part Curriculum part
     * @return JOOQ condition
     */
    private static Condition condition(
        final Integer grade,
        final String subject,
        final FederalCurriculumRequirement.PartType part
    ) {
        Condition condition = DSL.trueCondition();
        if (grade != null) {
            condition = condition.and(FederalCurriculumHtmlController.REQUIREMENT.GRADE.eq(grade));
        }
        if (subject != null && !subject.isBlank()) {
            condition = condition.and(
                FederalCurriculumHtmlController.REQUIREMENT.SUBJECT_NAME.likeIgnoreCase(
                    String.format("%%%s%%", subject.trim())
                )
            );
        }
        if (part != null) {
            condition = condition.and(
                FederalCurriculumHtmlController.REQUIREMENT.PART_TYPE.eq(
                    CurriculumPartType.valueOf(part.name())
                )
            );
        }
        return condition;
    }
}
