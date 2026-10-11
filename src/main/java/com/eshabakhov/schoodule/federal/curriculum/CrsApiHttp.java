/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

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
@PreAuthorize("hasRole('ADMIN')")
public class CrsApiHttp implements Curriculums {

    /**
     * Origin curriculums.
     */
    private final Curriculums origin;

    CrsApiHttp(final DSLContext ctx) {
        this.origin = new CrsPostgres(ctx);
    }

    @Override
    public Curriculum create(
        final String title,
        final Curriculum.Level level,
        final Curriculum.Week week,
        final String version,
        final String year,
        final String description
    ) throws Exception {
        return this.origin.create(title, level, week, version, year, description);
    }

    @Override
    public Curriculum curriculum(final long id) throws Exception {
        return this.origin.curriculum(id);
    }

    @Override
    public Curriculums selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, page, sorts);
    }

    @Override
    public Iterable<Curriculum> iterate() {
        return this.origin.iterate();
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return this.origin.print(media);
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }

    /**
     * Creates a federal curriculum.
     *
     * @param version Representation version
     * @param request Curriculum data
     * @return Created curriculum
     * @throws Exception If creation fails
     * @since 0.0.1
     */
    @PostMapping
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
        @RequestHeader("version") final LoadVersion version,
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
        final Curriculum pgcur = this.create(
            request.required("title").asText(),
            Curriculum.Level.valueOf(request.required("level").asText()),
            Curriculum.Week.valueOf(request.required("week").asText()),
            request.required("version").asText(),
            request.required("year").asText(),
            request.get("description").asText("")
        );
        return switch (version) {
            case SIMPLE -> {
                yield ResponseEntity.created(
                    URI.create(
                        String.format("/api/federal/curriculums/%d", pgcur.uid())
                    )
                ).contentType(
                    MediaType.valueOf(
                        "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                    )
                ).body(new CrSimple(pgcur).print(new MdJson()).print());
            }
            case FULL -> {
                yield ResponseEntity.created(
                    URI.create(
                        String.format("/api/federal/curriculums/%d", pgcur.uid())
                    )
                ).contentType(
                    MediaType.valueOf(
                        "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
                    )
                ).body(new CrFull(pgcur).print(new MdJson()).print());
            }
        };
    }

    /**
     * Updates or creates a federal curriculum.
     *
     * @param version Representation version
     * @param curriculum Curriculum ID
     * @param request Curriculum data
     * @return Updated or created curriculum
     * @throws Exception If persistence fails
     * @since 0.0.1
     */
    @PutMapping("/{curriculum}")
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
        @RequestHeader("version") final LoadVersion version,
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
        try {
            final Curriculum changed = this.curriculum(curriculum)
                .retitled(request.required("title").asText()).releveled(
                    Curriculum.Level.valueOf(request.required("level").asText())
                ).reweeked(
                    Curriculum.Week.valueOf(request.required("week").asText())
                )
                .reversioned(request.required("version").asText())
                .reyeared(request.required("year").asText())
                .redescriptioned(request.get("description").asText(""));
            response = switch (version) {
                case SIMPLE -> ResponseEntity
                    .status(HttpStatus.OK).contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                        )
                    )
                    .body(changed.print(new MdJson()).print());
                case FULL -> ResponseEntity
                    .status(HttpStatus.OK).contentType(
                        MediaType.valueOf(
                            "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
                        )
                    )
                    .body(changed.print(new MdJson()).print());
            };
        } catch (final CurriculumNotFoundException ignored) {
            final Curriculum created = this.create(
                request.required("title").asText(),
                Curriculum.Level.valueOf(request.required("level").asText()),
                Curriculum.Week.valueOf(request.required("week").asText()),
                request.required("version").asText(),
                request.required("year").asText(),
                request.get("description").asText("")
            );
            response = switch (version) {
                case SIMPLE -> ResponseEntity.created(
                    URI.create(String.format("/api/federal/curriculums/%d", created.uid()))
                ).contentType(
                    MediaType.valueOf(
                        "application/com.eshabakhov.schoodule.school.federal.curriculum.simple+json"
                    )
                ).body(created.print(new MdJson()).print());
                case FULL -> ResponseEntity.created(
                    URI.create(String.format("/api/federal/curriculums/%d", created.uid()))
                ).contentType(
                    MediaType.valueOf(
                        "application/com.eshabakhov.schoodule.school.federal.curriculum.full+json"
                    )
                ).body(created.print(new MdJson()).print());
            };
        }
        return response;
    }

    /**
     * Removes a federal curriculum.
     *
     * @param curriculum Curriculum ID
     * @return Empty response
     * @throws Exception If removal fails
     * @since 0.0.1
     */
    @DeleteMapping("/{curriculum}")
    @Operation(summary = "Remove federal curriculum")
    public ResponseEntity<Void> delete(
        @PathVariable final long curriculum
    ) throws Exception {
        this.remove(curriculum);
        return ResponseEntity.noContent().build();
    }

    /**
     * Federal curriculum accept version.
     */
    enum LoadVersion {

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
