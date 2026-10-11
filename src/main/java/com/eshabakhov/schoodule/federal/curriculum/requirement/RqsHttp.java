/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Header;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.federal.curriculum.CrsPostgres;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.annotation.RequestScope;

/**
 * HTTP and domain implementation of {@link Requirements}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestScope
@RequestMapping("/federal/curriculums/{curriculum}/requirements")
@PreAuthorize("hasRole('ADMIN')")
public class RqsHttp implements Requirements {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * Federal curriculum ID for the current request.
     */
    private long curriculum;

    /**
     * New controller.
     *
     * @param ctx JOOQ context
     * @since 0.0.1
     */
    public RqsHttp(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Requirement create(
        final Integer grade,
        final String subject,
        final Integer hours,
        final Requirement.PartType part
    ) throws Exception {
        return new RqsPostgres(this.ctx, this.curriculum)
            .create(grade, subject, hours, part);
    }

    @Override
    public Requirement requirement(final long id) throws Exception {
        return new RqsPostgres(this.ctx, this.curriculum).requirement(id);
    }

    @Override
    public Requirements selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return new RqsPostgres(this.ctx, this.curriculum)
            .selection(filters, page, sorts);
    }

    @Override
    public Iterable<Requirement> iterate() {
        return new RqsPostgres(this.ctx, this.curriculum).iterate();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return new RqsPostgres(this.ctx, this.curriculum).print(media);
    }

    @Override
    public void remove(final long id) throws Exception {
        new RqsPostgres(this.ctx, this.curriculum).remove(id);
    }

    /**
     * Shows curriculum requirements.
     *
     * @param current Curriculum ID
     * @param filters Filtering
     * @param page Pagination
     * @param sorts Sorting
     * @param header Representation header
     * @return Printed requirements
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-requirements.v1+json",
            "application/vnd.schoodule.federal-curriculum-requirements.v1+xml"
        }
    )
    public Object list(
        @PathVariable("curriculum") final long current,
        final Filters filters,
        final Page page,
        final Sorts sorts,
        final Header<?> header
    ) throws Exception {
        this.curriculum = current;
        return this.selection(filters, page, sorts).print(
            new CrsPostgres(this.ctx).curriculum(this.curriculum).print(
                header.media(
                    "federal-curriculums/requirements",
                    ""
                )
            )
        ).title("Требования: %s").attributes(
            RqsHttp.requirementsModel(this.curriculum, sorts)
        ).print();
    }

    /**
     * Shows the curriculum requirements fragment.
     *
     * @param current Curriculum ID
     * @param filters Filtering
     * @param page Pagination
     * @param sorts Sorting
     * @param header Representation header
     * @return Printed fragment
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public Object fragment(
        @PathVariable("curriculum") final long current,
        final Filters filters,
        final Page page,
        final Sorts sorts,
        final Header<?> header
    ) throws Exception {
        this.curriculum = current;
        return this.selection(filters, page, sorts).print(
            header.media(
                "federal-curriculums/requirements :: requirements-results",
                ""
            )
        ).attributes(
            RqsHttp.requirementsModel(this.curriculum, sorts)
        ).print();
    }

    /**
     * Shows one curriculum requirement.
     *
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @param header Representation header
     * @return Printed requirement
     * @throws Exception If lookup fails
     */
    @ResponseBody
    @GetMapping(
        value = "/{requirement}",
        produces = {
            "application/vnd.schoodule.federal-curriculum-requirement.simple.v1+json",
            "application/vnd.schoodule.federal-curriculum-requirement.simple.v1+xml"
        }
    )
    public Object details(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement,
        final Header<?> header
    ) throws Exception {
        this.curriculum = current;
        return this.requirement(requirement)
            .print(header.media("", ""))
            .print();
    }

    /**
     * Creates a curriculum requirement from form values.
     *
     * @param current Curriculum ID
     * @param grade Grade
     * @param subject Subject
     * @param hours Weekly hours
     * @param part Curriculum part
     * @return Redirect to requirements
     * @throws Exception If creation fails
     */
    @PostMapping("/create")
    public String created(
        @PathVariable("curriculum") final long current,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        this.curriculum = current;
        this.create(grade, subject.trim(), hours, part);
        return String.format(
            "redirect:/federal/curriculums/%d/requirements",
            this.curriculum
        );
    }

    /**
     * Updates a curriculum requirement from form values.
     *
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @param grade Grade
     * @param subject Subject
     * @param hours Weekly hours
     * @param part Curriculum part
     * @return Redirect to requirements
     * @throws Exception If update fails
     */
    @PostMapping("/{requirement}/edit")
    public String edited(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        this.curriculum = current;
        this.requirement(requirement)
            .regraded(grade)
            .resubjected(subject.trim())
            .reweekled(hours)
            .reparted(part);
        return String.format(
            "redirect:/federal/curriculums/%d/requirements",
            this.curriculum
        );
    }

    private static Map<String, Object> requirementsModel(
        final long curriculum,
        final Sorts sorts
    ) {
        String grade = "";
        String subject = "";
        String hours = "";
        String part = "";
        for (final Sort item : sorts.sorts()) {
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
            Map.entry("partTypes", Requirement.PartType.values()),
            Map.entry("gradeSort", grade),
            Map.entry("subjectSort", subject),
            Map.entry("hoursSort", hours),
            Map.entry("partSort", part)
        );
    }
}
