/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.curriculum;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.media.JsonMedia;
import com.eshabakhov.schoodule.page.PageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    /** JOOQ DSL context for executing database queries.*/
    private final DSLContext ctx;

    FederalCurriculumController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create federal curriculum")
    public ResponseEntity<ObjectNode> create(
        @RequestHeader("version") final CurriculumVersion version,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (!CurriculumVersion.SIMPLE.equals(version)) {
            throw new VersionHeaderException(version.name());
        }
        final JsonNode description = request.get("description");
        final String desc;
        if (description == null) {
            desc = null;
        } else {
            desc = description.asText();
        }
        final FederalCurriculum curriculum = new FcsPostgres(this.ctx)
            .create(
                FederalCurriculumController.required(request, "title").asText(),
                FederalCurriculum.Level.valueOf(
                    FederalCurriculumController.required(request, "level").asText()
                ),
                FederalCurriculum.Week.valueOf(
                    FederalCurriculumController.required(request, "week").asText()
                ),
                FederalCurriculumController.required(request, "version").asText(),
                FederalCurriculumController.required(request, "year").asText(),
                desc
            );
        final JsonMedia media = new JsonMedia();
        curriculum.print(media);
        return ResponseEntity
            .created(URI.create(String.format("/api/federal-curriculums/%d", curriculum.uid())))
            .body(media.json());
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
    public ResponseEntity<ObjectNode> list(
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
        final PageableList<FederalCurriculum> result =
            new FcsPostgres(this.ctx).curriculums(condition, new PageRequest(limit, offset));
        final ArrayNode items = JsonNodeFactory.instance.arrayNode();
        result.list().forEach(
            fc -> {
                final JsonMedia media = new JsonMedia();
                fc.print(media);
                items.add(media.json());
            }
        );
        final ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.set("items", items);
        response.put("total", result.total());
        return ResponseEntity.ok(response);
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
    public ResponseEntity<ObjectNode> get(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum
    ) throws Exception {
        if (!CurriculumVersion.SIMPLE.equals(version)) {
            throw new VersionHeaderException(version.name());
        }
        final JsonMedia media = new JsonMedia();
        new FcsPostgres(this.ctx).curriculum(curriculum).print(media);
        return ResponseEntity.ok(media.json());
    }

    @PutMapping("/{curriculum}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update federal curriculum")
    public ResponseEntity<ObjectNode> put(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (!CurriculumVersion.SIMPLE.equals(version)) {
            throw new VersionHeaderException(version.name());
        }
        final JsonNode description = request.get("description");
        final String desc;
        if (description == null) {
            desc = null;
        } else {
            desc = description.asText();
        }
        final FederalCurriculum updated = new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .retitled(FederalCurriculumController.required(request, "title").asText())
            .releveled(
                FederalCurriculum.Level.valueOf(
                    FederalCurriculumController.required(request, "level").asText()
                )
            )
            .reweeked(
                FederalCurriculum.Week.valueOf(
                    FederalCurriculumController.required(request, "week").asText()
                )
            )
            .reversioned(FederalCurriculumController.required(request, "version").asText())
            .reyeared(FederalCurriculumController.required(request, "year").asText())
            .redescriptioned(desc);
        final JsonMedia media = new JsonMedia();
        updated.print(media);
        final ResponseEntity<ObjectNode> response;
        if (curriculum == updated.uid()) {
            response = ResponseEntity.ok(media.json());
        } else {
            response = ResponseEntity
                .created(URI.create(String.format("/api/federal-curriculums/%d", updated.uid())))
                .body(media.json());
        }
        return response;
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
    public ResponseEntity<ObjectNode> createRequirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (!CurriculumVersion.SIMPLE.equals(version)) {
            throw new VersionHeaderException(version.name());
        }
        final FederalCurriculumRequirement requirement = new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .create(
                FederalCurriculumController.required(request, "grade").asInt(),
                FederalCurriculumController.required(request, "subjectName").asText(),
                FederalCurriculumController.required(request, "weeklyHours").asInt(),
                FederalCurriculumRequirement.PartType.valueOf(
                    FederalCurriculumController.required(request, "partType").asText()
                )
            );
        final JsonMedia media = new JsonMedia();
        requirement.print(media);
        return ResponseEntity
            .created(
                URI.create(
                    String.format(
                        "/api/federal-curriculums/%d/requirements/%d",
                        curriculum, requirement.uid()
                    )
                )
            )
            .body(media.json());
    }

    @GetMapping("/{curriculum}/requirements")
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
                'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
            )
        """
    )
    @Operation(summary = "Fetch federal curriculum requirements")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<ObjectNode> requirements(
        @PathVariable final long curriculum,
        @RequestParam(name = "limit", required = false, defaultValue = "10") final int limit,
        @RequestParam(name = "offset", required = false, defaultValue = "1") final int offset,
        @RequestParam(name = "grade", required = false) final Integer grade
    ) throws Exception {
        Condition condition = DSL.trueCondition();
        if (grade != null) {
            condition = condition.and(
                FederalCurriculumController.REQUIREMENT.GRADE.eq(grade)
            );
        }
        final PageableList<FederalCurriculumRequirement> result = new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirements(condition, new PageRequest(limit, offset));
        final ArrayNode items = JsonNodeFactory.instance.arrayNode();
        result.list().forEach(
            req -> {
                final JsonMedia media = new JsonMedia();
                req.print(media);
                items.add(media.json());
            }
        );
        final ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.set("items", items);
        response.put("total", result.total());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{curriculum}/requirements/{requirement}")
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
                'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
            )
        """
    )
    @Operation(summary = "Fetch federal curriculum requirement")
    public ResponseEntity<ObjectNode> requirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @PathVariable final long requirement
    ) throws Exception {
        if (!CurriculumVersion.SIMPLE.equals(version)) {
            throw new VersionHeaderException(version.name());
        }
        final JsonMedia media = new JsonMedia();
        new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirement(requirement)
            .print(media);
        return ResponseEntity.ok(media.json());
    }

    @PutMapping("/{curriculum}/requirements/{requirement}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update federal curriculum requirement")
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<ObjectNode> putRequirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @PathVariable final long requirement,
        @RequestBody final JsonNode request
    ) throws Exception {
        if (!CurriculumVersion.SIMPLE.equals(version)) {
            throw new VersionHeaderException(version.name());
        }
        final FederalCurriculumRequirement updated = new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirement(requirement)
            .regraded(FederalCurriculumController.required(request, "grade").asInt())
            .resubjected(FederalCurriculumController.required(request, "subjectName").asText())
            .reweekled(FederalCurriculumController.required(request, "weeklyHours").asInt())
            .reparted(
                FederalCurriculumRequirement.PartType.valueOf(
                    FederalCurriculumController.required(request, "partType").asText()
                )
            );
        final JsonMedia media = new JsonMedia();
        updated.print(media);
        final ResponseEntity<ObjectNode> response;
        if (requirement == updated.uid()) {
            response = ResponseEntity.ok(media.json());
        } else {
            response = ResponseEntity
                .created(
                    URI.create(
                        String.format(
                            "/api/federal-curriculums/%d/requirements/%d",
                            curriculum, updated.uid()
                        )
                    )
                )
                .body(media.json());
        }
        return response;
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

    private static JsonNode required(final JsonNode request, final String field)
        throws CurriculumRequiredFieldException {
        final var value = request.get(field);
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

    /**
     * Federal curriculum accept version.
     */
    public enum CurriculumVersion {
        /**
         * Version of simple federal curriculum.
         */
        SIMPLE
    }
}
