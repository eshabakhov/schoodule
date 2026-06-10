/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.federal.FederalCurriculum;
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
 * Federal curriculum REST API controller.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle DesignForExtensionCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/federal/curriculums")
@Tag(name = "Federal Curriculums")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidFieldNameMatchingMethodName"})
public class FederalCurriculumController {

    /** JOOQ Table for FederalCurriculum. */
    private static final com.eshabakhov.schoodule.tables.FederalCurriculum CURRICULUM =
        com.eshabakhov.schoodule.tables.FederalCurriculum.FEDERAL_CURRICULUM;

    /** JOOQ DSL context for executing database queries.*/
    private final DSLContext ctx;

    FederalCurriculumController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create federal curriculum",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Federal curriculum",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = { "SIMPLE", "FULL" }
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "201",
        description = "Federal curriculum created",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027"
                        }"""
                )
            ),
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json",
                examples = @ExampleObject(
                    name = "Full federal curriculum",
                    summary = "Full",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027",
                            "description": "Федеральный учебный план
                                образовательных организаций,
                                реализующих образовательную программу
                                основного общего образования,
                                обеспечивает реализацию требований ФГОС ООО,
                                определяет общие рамки отбора учебного материала,
                                формирования перечня результатов образования
                                и организации образовательной деятельности."
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
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request federal curriculum",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "title": "Federal curriculum for basic general education",
                                "level": "BASIC",
                                "week": "FIVE_DAYS",
                                "version": "1",
                                "year": "2026/2027",
                                "description": "Федеральный учебный план
                                    образовательных организаций,
                                    реализующих образовательную программу
                                    основного общего образования,
                                    обеспечивает реализацию требований ФГОС ООО,
                                    определяет общие рамки отбора учебного материала,
                                    формирования перечня результатов образования
                                    и организации образовательной деятельности."
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        final FederalCurriculum pgcur = new FcsPostgres(this.ctx)
            .create(
                request.required("title").asText(),
                FederalCurriculum.Level.valueOf(request.required("level").asText()),
                FederalCurriculum.Week.valueOf(request.required("week").asText()),
                request.required("version").asText(),
                request.required("year").asText(),
                request.get("description").asText("")
            );
        final JsonMedia media = new JsonMedia();
        return switch (version) {
            case SIMPLE -> {
                new FcSimple(pgcur).print(media);
                yield ResponseEntity
                    .created(URI.create(String.format("/api/federal/curriculums/%d", pgcur.uid())))
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                        )
                    )
                    .body(media.json());
            }
            case FULL -> {
                new FcFull(pgcur).print(media);
                yield ResponseEntity
                    .created(URI.create(String.format("/api/federal/curriculums/%d", pgcur.uid())))
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
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
    @Operation(
        summary = "Fetch federal curriculum",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Federal curriculum",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = { "SIMPLE", "FULL" }
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum fetched",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027"
                        }"""
                )
            ),
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json",
                examples = @ExampleObject(
                    name = "Full federal curriculum",
                    summary = "Full",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027",
                            "description": "Федеральный учебный план
                                образовательных организаций,
                                реализующих образовательную программу
                                основного общего образования,
                                обеспечивает реализацию требований ФГОС ООО,
                                определяет общие рамки отбора учебного материала,
                                формирования перечня результатов образования
                                и организации образовательной деятельности."
                        }"""
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "404",
        description = "Federal curriculum not found",
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
    public ResponseEntity<ObjectNode> get(
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum
    ) throws Exception {
        final FederalCurriculum pgcur = new FcsPostgres(this.ctx).curriculum(curriculum);
        final JsonMedia media = new JsonMedia();
        return switch (version) {
            case SIMPLE -> {
                new FcSimple(pgcur).print(media);
                yield ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                        )
                    )
                    .body(media.json());
            }
            case FULL -> {
                new FcFull(pgcur).print(media);
                yield ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
                        )
                    )
                    .body(media.json());
            }
        };
    }

    @PutMapping("/{curriculum}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update federal curriculum",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Federal curriculum",
                required = true,
                schema = @Schema(
                    type = "string",
                    allowableValues = { "SIMPLE", "FULL" }
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum updated",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027"
                        }"""
                )
            ),
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json",
                examples = @ExampleObject(
                    name = "Full federal curriculum",
                    summary = "Full",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027",
                            "description": "Федеральный учебный план
                                образовательных организаций,
                                реализующих образовательную программу
                                основного общего образования,
                                обеспечивает реализацию требований ФГОС ООО,
                                определяет общие рамки отбора учебного материала,
                                формирования перечня результатов образования
                                и организации образовательной деятельности."
                        }"""
                )
            )
        }
    )
    @ApiResponse(
        responseCode = "201",
        description = "Federal curriculum created",
        content = {
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json",
                examples = @ExampleObject(
                    name = "Simple federal curriculum",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027"
                        }"""
                )
            ),
            @Content(
                mediaType =
                    "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json",
                examples = @ExampleObject(
                    name = "Full federal curriculum",
                    summary = "Full",
                    value = """
                        {
                            "id": 1,
                            "title": "First federal curriculum",
                            "level": "BASIC",
                            "week": "FIVE_DAYS",
                            "version": "1",
                            "year": "2026/2027",
                            "description": "Федеральный учебный план
                                образовательных организаций,
                                реализующих образовательную программу
                                основного общего образования,
                                обеспечивает реализацию требований ФГОС ООО,
                                определяет общие рамки отбора учебного материала,
                                формирования перечня результатов образования
                                и организации образовательной деятельности."
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
        @RequestHeader("version") final CurriculumVersion version,
        @PathVariable final long curriculum,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request federal curriculum",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "title": "Federal curriculum for basic general education",
                                "level": "BASIC",
                                "week": "FIVE_DAYS",
                                "version": "1",
                                "year": "2026/2027",
                                "description": "Федеральный учебный план
                                    образовательных организаций,
                                    реализующих образовательную программу
                                    основного общего образования,
                                    обеспечивает реализацию требований ФГОС ООО,
                                    определяет общие рамки отбора учебного материала,
                                    формирования перечня результатов образования
                                    и организации образовательной деятельности."
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        ResponseEntity<ObjectNode> response;
        final JsonMedia media = new JsonMedia();
        try {
            new FcsPostgres(this.ctx).curriculum(curriculum)
                .retitled(request.required("title").asText())
                .releveled(
                    FederalCurriculum.Level.valueOf(request.required("level").asText())
                )
                .reweeked(
                    FederalCurriculum.Week.valueOf(request.required("week").asText())
                )
                .reversioned(request.required("version").asText())
                .reyeared(request.required("year").asText())
                .redescriptioned(request.get("description").asText(""))
                .print(media);
            response = switch (version) {
                case SIMPLE -> ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                        )
                    )
                    .body(media.json());
                case FULL -> ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
                        )
                    )
                    .body(media.json());
            };
        } catch (final FcsPostgres.CurriculumNotFoundException ex) {
            final var created = new FcsPostgres(this.ctx)
                .create(
                    request.required("title").asText(),
                    FederalCurriculum.Level.valueOf(request.required("level").asText()),
                    FederalCurriculum.Week.valueOf(request.required("week").asText()),
                    request.required("version").asText(),
                    request.required("year").asText(),
                    request.get("description").asText("")
                );
            created.print(media);
            response = switch (version) {
                case SIMPLE -> ResponseEntity
                    .created(
                        URI.create(String.format("/api/federal/curriculums/%d", created.uid()))
                    )
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                        )
                    )
                    .body(media.json());
                case FULL -> ResponseEntity
                    .created(
                        URI.create(String.format("/api/federal/curriculums/%d", created.uid()))
                    )
                    .contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
                        )
                    )
                    .body(media.json());
            };
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

    /**
     * Federal curriculum accept version.
     */
    enum CurriculumVersion {
        /**
         * Version of simple federal curriculum.
         */
        SIMPLE,

        /**
         * Version of full federal curriculum.
         */
        FULL
    }
}
