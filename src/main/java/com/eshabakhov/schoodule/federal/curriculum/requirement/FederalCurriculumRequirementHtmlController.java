/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.federal.curriculum.CrsPostgres;
import org.jooq.DSLContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for HTML responses for {@link Requirement}.
 *
 * @since 0.0.1
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums")
@PreAuthorize("hasRole('ADMIN')")
public class FederalCurriculumRequirementHtmlController {

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
    public FederalCurriculumRequirementHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Creates a curriculum requirement from form values.
     *
     * @param curriculum Curriculum ID
     * @param grade Grade
     * @param subject Subject
     * @param hours Weekly hours
     * @param part Curriculum part
     * @return Redirect to requirements
     * @throws Exception If creation fails
     * @since 0.0.1
     */
    @PostMapping("/{curriculum}/requirements/create")
    public String create(
        @PathVariable final long curriculum,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        new CrsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .create(grade, subject.trim(), hours, part);
        return String.format("redirect:/federal/curriculums/%d/requirements", curriculum);
    }

    /**
     * Updates a curriculum requirement from form values.
     *
     * @param curriculum Curriculum ID
     * @param requirement Requirement ID
     * @param grade Grade
     * @param subject Subject
     * @param hours Weekly hours
     * @param part Curriculum part
     * @return Redirect to requirements
     * @throws Exception If update fails
     * @since 0.0.1
     */
    @PostMapping("/{curriculum}/requirements/{requirement}/edit")
    public String edit(
        @PathVariable final long curriculum,
        @PathVariable final long requirement,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        new CrsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirement(requirement)
            .regraded(grade)
            .resubjected(subject.trim())
            .reweekled(hours)
            .reparted(part);
        return String.format("redirect:/federal/curriculums/%d/requirements", curriculum);
    }
}
