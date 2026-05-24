/*
 * Р’В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.curriculum.FcBase;
import com.eshabakhov.schoodule.curriculum.FcrBase;
import com.eshabakhov.schoodule.curriculum.FcsPostgres;
import com.eshabakhov.schoodule.curriculum.FederalCurriculum;
import com.eshabakhov.schoodule.curriculum.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Federal curriculum REST API controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/federal-curriculums")
@Tag(name = "Federal Curriculums")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidFieldNameMatchingMethodName"})
public class FederalCurriculumController {

    /** JOOQ Table for FederalCurriculum. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /** JOOQ Table for FederalCurriculumRequirement. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    FederalCurriculumController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create federal curriculum")
    public ResponseEntity<FederalCurriculum> create(
        @RequestHeader("version") final CurriculumVersion version,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (CurriculumVersion.SIMPLE.equals(version)) {
            final FederalCurriculum curriculum = new FcsPostgres(this.ctx)
                .create(FederalCurriculumController.curriculum(request, Long.MIN_VALUE));
            return ResponseEntity
                .created(
                    URI.create(
                        String.format(
                            "/api/federal-curriculums/%d",
                            curriculum.uid()
                        )
                    )
                )
                .body(curriculum);
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN',
                'DIRECTOR',
                'DEPUTY_DIRECTOR',
                'BASIC_MAKER',
                'ADVANCED_MAKER',
                'PRO_MAKER'
            )
        """
    )
    @Operation(summary = "Fetch list of federal curriculums")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<FederalCurriculum>> list(
        @RequestParam(name = "limit", required = false, defaultValue = "10") final int limit,
        @RequestParam(name = "offset", required = false, defaultValue = "1") final int offset,
        @RequestParam(value = "title_ct", required = false) final String title
    ) throws Exception {
        Condition condition = DSL.trueCondition();
        if (title != null && !title.isBlank()) {
            condition = condition.and(
                FederalCurriculumController.CURRICULUM.TITLE.likeIgnoreCase(
                    String.format("%%%s%%", title)
                )
            );
        }
        return ResponseEntity.ok().body(
            new FcsPostgres(this.ctx).curriculums(condition, new PageRequest(limit, offset))
        );
    }

    @GetMapping("/{curriculum}")
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN',
                'DIRECTOR',
                'DEPUTY_DIRECTOR',
                'BASIC_MAKER',
                'ADVANCED_MAKER',
                'PRO_MAKER'
            )
        """
    )
    @Operation(summary = "Fetch federal curriculum")
    public ResponseEntity<FederalCurriculum> get(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum
    ) throws Exception {
        if (CurriculumVersion.SIMPLE.equals(version)) {
            return ResponseEntity.ok().body(new FcsPostgres(this.ctx).curriculum(curriculum));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @PutMapping("/{curriculum}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update federal curriculum")
    public ResponseEntity<FederalCurriculum> put(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (CurriculumVersion.SIMPLE.equals(version)) {
            final FederalCurriculum updated = new FcsPostgres(this.ctx)
                .put(FederalCurriculumController.curriculum(request, curriculum));
            final ResponseEntity<FederalCurriculum> response;
            if (curriculum == updated.uid()) {
                response = ResponseEntity.ok().body(updated);
            } else {
                response = ResponseEntity.created(
                    URI.create(
                        String.format(
                            "/api/federal-curriculums/%d",
                            updated.uid()
                        )
                    )
                ).body(updated);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @DeleteMapping("/{curriculum}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove federal curriculum")
    public ResponseEntity<Void> delete(@PathVariable final long curriculum) throws Exception {
        new FcsPostgres(this.ctx).remove(curriculum);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{curriculum}/requirements")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create federal curriculum requirement")
    public ResponseEntity<FederalCurriculumRequirement> createRequirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (CurriculumVersion.SIMPLE.equals(version)) {
            final FederalCurriculumRequirement requirement = new FcsPostgres(this.ctx)
                .curriculum(curriculum)
                .requirements()
                .create(FederalCurriculumController.requirement(request, Long.MIN_VALUE));
            return ResponseEntity
                .created(
                    URI.create(
                        String.format(
                            "/api/federal-curriculums/%d/requirements/%d",
                            curriculum,
                            requirement.uid()
                        )
                    )
                )
                .body(requirement);
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping("/{curriculum}/requirements")
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN',
                'DIRECTOR',
                'DEPUTY_DIRECTOR',
                'BASIC_MAKER',
                'ADVANCED_MAKER',
                'PRO_MAKER'
            )
        """
    )
    @Operation(summary = "Fetch federal curriculum requirements")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<PageableList<FederalCurriculumRequirement>> requirements(
        @PathVariable final long curriculum,
        @RequestParam(name = "limit", required = false, defaultValue = "10") final int limit,
        @RequestParam(name = "offset", required = false, defaultValue = "1") final int offset,
        @RequestParam(name = "grade", required = false) final Integer grade
    ) throws Exception {
        Condition condition = DSL.trueCondition();
        if (grade != null) {
            condition = condition.and(FederalCurriculumController.REQUIREMENT.GRADE.eq(grade));
        }
        return ResponseEntity.ok().body(
            new FcsPostgres(this.ctx)
                .curriculum(curriculum)
                .requirements()
                .requirements(condition, new PageRequest(limit, offset))
        );
    }

    @GetMapping("/{curriculum}/requirements/{requirement}")
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN',
                'DIRECTOR',
                'DEPUTY_DIRECTOR',
                'BASIC_MAKER',
                'ADVANCED_MAKER',
                'PRO_MAKER'
            )
        """
    )
    @Operation(summary = "Fetch federal curriculum requirement")
    public ResponseEntity<FederalCurriculumRequirement> requirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @PathVariable final long requirement
    ) throws Exception {
        if (CurriculumVersion.SIMPLE.equals(version)) {
            return ResponseEntity.ok().body(
                new FcsPostgres(this.ctx)
                    .curriculum(curriculum)
                    .requirements()
                    .requirement(requirement)
            );
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @PutMapping("/{curriculum}/requirements/{requirement}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update federal curriculum requirement")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<FederalCurriculumRequirement> putRequirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @PathVariable final long requirement,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (CurriculumVersion.SIMPLE.equals(version)) {
            final FederalCurriculumRequirement updated = new FcsPostgres(this.ctx)
                .curriculum(curriculum)
                .requirements()
                .put(FederalCurriculumController.requirement(request, requirement));
            final ResponseEntity<FederalCurriculumRequirement> response;
            if (requirement == updated.uid()) {
                response = ResponseEntity.ok().body(updated);
            } else {
                response = ResponseEntity.created(
                    URI.create(
                        String.format(
                            "/api/federal-curriculums/%d/requirements/%d",
                            curriculum,
                            updated.uid()
                        )
                    )
                ).body(updated);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @DeleteMapping("/{curriculum}/requirements/{requirement}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove federal curriculum requirement")
    public ResponseEntity<Void> deleteRequirement(
        @PathVariable final long curriculum,
        @PathVariable final long requirement
    ) throws Exception {
        new FcsPostgres(this.ctx).curriculum(curriculum).requirements().remove(requirement);
        return ResponseEntity.noContent().build();
    }

    private static FederalCurriculum curriculum(
        final JsonNode request,
        final long id
    ) throws CurriculumRequiredFieldException {
        final JsonNode title = FederalCurriculumController.required(request, "title");
        final JsonNode level = FederalCurriculumController.required(request, "educationLevel");
        final JsonNode week = FederalCurriculumController.required(request, "studyWeekType");
        final JsonNode version = FederalCurriculumController.required(request, "version");
        final JsonNode year = FederalCurriculumController.required(request, "academicYear");
        final JsonNode description = request.get("description");
        final String desc;
        if (description == null) {
            desc = null;
        } else {
            desc = description.asText();
        }
        return new FcBase(
            id,
            title.asText(),
            FederalCurriculum.EducationLevel.valueOf(level.asText()),
            FederalCurriculum.StudyWeek.valueOf(week.asText()),
            version.asText(),
            year.asText(),
            desc
        );
    }

    private static FederalCurriculumRequirement requirement(
        final JsonNode request,
        final long id
    ) throws CurriculumRequiredFieldException {
        final JsonNode grade = FederalCurriculumController.required(request, "grade");
        final JsonNode subject = FederalCurriculumController.required(request, "subjectName");
        final JsonNode hours = FederalCurriculumController.required(request, "weeklyHours");
        final JsonNode part = FederalCurriculumController.required(request, "partType");
        return new FcrBase(
            id,
            null,
            grade.asInt(),
            subject.asText(),
            hours.asInt(),
            FederalCurriculumRequirement.PartType.valueOf(part.asText())
        );
    }

    private static JsonNode required(
        final JsonNode request,
        final String field
    ) throws CurriculumRequiredFieldException {
        final JsonNode value = request.get(field);
        if (value == null || value.asText().isBlank()) {
            throw new CurriculumRequiredFieldException(
                String.format("Field '%s' is required and cannot be empty", field)
            );
        }
        return value;
    }

    public static class CurriculumRequiredFieldException extends Exception {
        public CurriculumRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** Federal curriculum accept version. */
    public enum CurriculumVersion {

        /** Version of simple federal curriculum. */
        SIMPLE
    }
}
