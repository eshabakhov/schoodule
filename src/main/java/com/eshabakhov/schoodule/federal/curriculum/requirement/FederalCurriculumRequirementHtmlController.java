/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.federal.curriculum.FcsPostgres;
import org.jooq.DSLContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for HTML responses for {@link FederalCurriculumRequirement}.
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
public class FederalCurriculumRequirementHtmlController {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    public FederalCurriculumRequirementHtmlController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping("/{curriculum}/requirements/create")
    public String create(
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
        return String.format("redirect:/federal/curriculums/%d", curriculum);
    }

    @PostMapping("/{curriculum}/requirements/{requirement}/edit")
    public String edit(
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
        return String.format("redirect:/federal/curriculums/%d", curriculum);
    }
}
