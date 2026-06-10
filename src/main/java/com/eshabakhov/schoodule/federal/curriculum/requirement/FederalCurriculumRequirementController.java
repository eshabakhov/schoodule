/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.federal.curriculum.FcsPostgres;
import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.media.JsonMedia;
import com.eshabakhov.schoodule.page.PageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * Federal curriculum requirement REST API controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/federal/curriculums/{curriculum}/requirements")
@Tag(name = "Federal Curriculums")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidFieldNameMatchingMethodName"})
public class FederalCurriculumRequirementController {

    /** JOOQ Table for FederalCurriculumRequirement. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculumRequirement REQUIREMENT =
        com.eshabakhov.schoodule.tables.FederalCurriculumRequirement.FEDERAL_CURRICULUM_REQUIREMENT;

    /** JOOQ DSL context for executing database queries.*/
    private final DSLContext ctx;

    FederalCurriculumRequirementController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create federal curriculum requirement",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Federal curriculum requirement",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = "SIMPLE"
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "201",
        description = "Federal curriculum requirement created",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum requirement",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "grade": 5,
                            "subjectName": "Математика",
                            "weeklyHours": 3,
                            "partType": "MANDATORY"
                        }"""
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "406",
        description = "Not acceptable header",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Version header is incorrect",
                    summary = "Incorrect header",
                    value = """
                        {
                            "message": "Method parameter 'version' is incorrect",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    public ResponseEntity<ObjectNode> create(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request federal curriculum requirement",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "grade": 5,
                                "subjectName": "Математика",
                                "weeklyHours": 3,
                                "partType": "MANDATORY"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        final FederalCurriculumRequirement requirement = new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .create(
                request.required("grade").asInt(),
                request.required("subjectName").asText(),
                request.required("weeklyHours").asInt(),
                FederalCurriculumRequirement.PartType.valueOf(
                    request.required("partType").asText()
                )
            );
        final JsonMedia media = new JsonMedia();
        return switch (version) {
            case SIMPLE -> {
                new FcrSimple(requirement).print(media);
                yield ResponseEntity
                    .created(
                        URI.create(
                            String.format(
                                "/api/federal/curriculums/%d/requirements/%d",
                                curriculum, requirement.uid()
                            )
                        )
                    )
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                        )
                    )
                    .body(media.json());
            }
        };
    }

    @GetMapping
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
                FederalCurriculumRequirementController.REQUIREMENT.GRADE.eq(grade)
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

    @GetMapping("/{requirement}")
    @PreAuthorize(
        """
            hasAnyRole(
                'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
                'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
            )
        """
    )
    @Operation(
        summary = "Fetch federal curriculum requirement",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Federal curriculum requirement",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = "SIMPLE"
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum requirement fetched",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum requirement",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "grade": 5,
                            "subjectName": "Математика",
                            "weeklyHours": 3,
                            "partType": "MANDATORY"
                        }"""
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "404",
        description = "Federal curriculum requirement not found",
        content = @Content(mediaType = "application/json")
    )
    @ApiResponse(
        responseCode = "406",
        description = "Not acceptable header",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Version header is incorrect",
                    summary = "Incorrect header",
                    value = """
                        {
                            "message": "Method parameter 'version' is incorrect",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    public ResponseEntity<ObjectNode> requirement(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @PathVariable final long requirement
    ) throws Exception {
        final JsonMedia media = new JsonMedia();
        final FederalCurriculumRequirement fcr = new FcsPostgres(this.ctx)
            .curriculum(curriculum)
            .requirements()
            .requirement(requirement);
        return switch (version) {
            case SIMPLE -> {
                new FcrSimple(fcr).print(media);
                yield ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                        )
                    )
                    .body(media.json());
            }
        };
    }

    @PutMapping("/{requirement}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update federal curriculum requirement",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Federal curriculum requirement",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = "SIMPLE"
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum requirement updated",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum requirement",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "grade": 5,
                            "subjectName": "Математика",
                            "weeklyHours": 3,
                            "partType": "MANDATORY"
                        }"""
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "201",
        description = "Federal curriculum requirement created",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum requirement",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "grade": 5,
                            "subjectName": "Математика",
                            "weeklyHours": 3,
                            "partType": "MANDATORY"
                        }"""
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "406",
        description = "Not acceptable header",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Version header is incorrect",
                    summary = "Incorrect header",
                    value = """
                        {
                            "message": "Method parameter 'version' is incorrect",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
    )
    //@checkstyle ParameterNumberCheck (1 line)
    public ResponseEntity<ObjectNode> put(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @PathVariable final long requirement,
        @RequestBody final JsonNode request
    ) throws Exception {
        ResponseEntity<ObjectNode> response;
        final JsonMedia media = new JsonMedia();
        try {
            final FederalCurriculumRequirement fcr = new FcsPostgres(this.ctx)
                .curriculum(curriculum)
                .requirements()
                .requirement(requirement)
                .regraded(request.required("grade").asInt())
                .resubjected(request.required("subjectName").asText())
                .reweekled(request.required("weeklyHours").asInt())
                .reparted(
                    FederalCurriculumRequirement.PartType.valueOf(
                        request.required("partType").asText()
                    )
                );
            response = switch (version) {
                case SIMPLE -> {
                    new FcrSimple(fcr).print(media);
                    yield ResponseEntity
                        .status(HttpStatus.OK)
                        .contentType(
                            MediaType.valueOf(
                                "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                            )
                        )
                        .body(media.json());
                }
            };
        } catch (final FcrsPostgres.RequirementNotFoundException ex) {
            final var created =  new FcsPostgres(this.ctx)
                .curriculum(curriculum)
                .requirements()
                .create(
                    request.required("grade").asInt(),
                    request.required("subjectName").asText(),
                    request.required("weeklyHours").asInt(),
                    FederalCurriculumRequirement.PartType.valueOf(
                        request.required("partType").asText()
                    )
                );
            response = switch (version) {
                case SIMPLE -> {
                    new FcrSimple(created).print(media);
                    yield ResponseEntity
                        .created(
                            URI.create(
                                String.format(
                                    "/api/federal/curriculums/%d/requirements/%d",
                                    curriculum, created.uid()
                                )
                            )
                        )
                        .contentType(
                            MediaType.valueOf(
                                "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                            )
                        )
                        .body(media.json());
                }
            };
        }
        return response;
    }

    @DeleteMapping("/{requirement}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove federal curriculum requirement")
    public ResponseEntity<Void> delete(
        @PathVariable final long curriculum,
        @PathVariable final long requirement
    ) throws Exception {
        new FcsPostgres(this.ctx).curriculum(curriculum).requirements().remove(requirement);
        return ResponseEntity.noContent().build();
    }

    /**
     * Federal curriculum requirement accept version.
     */
    enum CurriculumVersion {
        /**
         * Version of simple federal curriculum requirement.
         */
        SIMPLE
    }
}
