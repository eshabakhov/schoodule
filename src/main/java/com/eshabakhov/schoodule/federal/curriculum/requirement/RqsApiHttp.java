/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Federal curriculum requirement REST API controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@RestController
@RequestScope
@RequestMapping("/api/federal/curriculums/{curriculum}/requirements")
@Tag(name = "Federal Curriculums")
@PreAuthorize("hasRole('ADMIN')")
public class RqsApiHttp implements Requirements {

    /**
     * JOOQ DSL context for executing database queries.
     */
    private final DSLContext ctx;

    /**
     * Federal curriculum ID for the current request.
     */
    private long curriculum;

    RqsApiHttp(final DSLContext ctx) {
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
     * Creates a curriculum requirement.
     *
     * @param version Representation version
     * @param current Curriculum ID
     * @param request Requirement data
     * @return Created requirement
     * @throws Exception If creation fails
     * @since 0.0.1
     */
    @PostMapping
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
        @RequestHeader("version") final LoadVersion version,
        @PathVariable("curriculum") final long current,
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
        this.curriculum = current;
        final Requirement requirement = this.create(
            request.required("grade").asInt(),
            request.required("subjectName").asText(),
            request.required("weeklyHours").asInt(),
            Requirement.PartType.valueOf(
                request.required("partType").asText()
            )
        );
        return switch (version) {
            case SIMPLE -> {
                yield ResponseEntity.created(
                    URI.create(
                        String.format(
                            "/api/federal/curriculums/%d/requirements/%d",
                            this.curriculum, requirement.uid()
                        )
                    )
                ).contentType(
                    MediaType.valueOf(
                        "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                    )
                ).body(new RqSimple(requirement).print(new MdJson()).print());
            }
        };
    }

    /**
     * Updates or creates a curriculum requirement.
     *
     * @param version Representation version
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @param request Requirement data
     * @return Updated or created requirement
     * @throws Exception If persistence fails
     * @since 0.0.1
     */
    @PutMapping("/{requirement}")
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
    public ResponseEntity<ObjectNode> put(
        @RequestHeader("version") final LoadVersion version,
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement,
        @RequestBody final JsonNode request
    ) throws Exception {
        this.curriculum = current;
        ResponseEntity<ObjectNode> response;
        try {
            response = switch (version) {
                case SIMPLE -> {
                    yield ResponseEntity
                        .status(HttpStatus.OK).contentType(
                            MediaType.valueOf(
                                "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                            )
                        ).body(
                            new RqSimple(
                                this.requirement(requirement)
                                    .regraded(request.required("grade").asInt()).resubjected(
                                        request.required("subjectName").asText()
                                    ).reweekled(
                                        request.required("weeklyHours").asInt()
                                    ).reparted(
                                        Requirement.PartType.valueOf(
                                            request.required("partType").asText()
                                        )
                                    )
                            ).print(new MdJson()).print()
                        );
                }
            };
        } catch (final RequirementNotFoundException ignored) {
            final Requirement created = this.create(
                request.required("grade").asInt(),
                request.required("subjectName").asText(),
                request.required("weeklyHours").asInt(),
                Requirement.PartType.valueOf(
                    request.required("partType").asText()
                )
            );
            response = switch (version) {
                case SIMPLE -> {
                    yield ResponseEntity.created(
                        URI.create(
                            String.format(
                                "/api/federal/curriculums/%d/requirements/%d",
                                this.curriculum, created.uid()
                            )
                        )
                    ).contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.requirement.simple+json"
                        )
                    ).body(
                        new RqSimple(created)
                            .print(new MdJson())
                            .print()
                    );
                }
            };
        }
        return response;
    }

    /**
     * Removes a curriculum requirement.
     *
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @return Empty response
     * @throws Exception If removal fails
     * @since 0.0.1
     */
    @DeleteMapping("/{requirement}")
    @Operation(summary = "Remove federal curriculum requirement")
    public ResponseEntity<Void> delete(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement
    ) throws Exception {
        this.curriculum = current;
        this.remove(requirement);
        return ResponseEntity.noContent().build();
    }

    /**
     * Federal curriculum requirement accept version.
     */
    enum LoadVersion {

        /**
         * Version of simple federal curriculum requirement.
         */
        SIMPLE
    }
}
