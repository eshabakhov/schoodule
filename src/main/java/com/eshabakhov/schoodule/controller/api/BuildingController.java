/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.api;

import com.eshabakhov.schoodule.PageableList;
import com.eshabakhov.schoodule.error.VersionHeaderException;
import com.eshabakhov.schoodule.page.PageRequest;
import com.eshabakhov.schoodule.school.Building;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.school.building.BdBase;
import com.eshabakhov.schoodule.school.building.BdsPostgres;
import com.fasterxml.jackson.databind.JsonNode;
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
 * Building's client controller.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@RestController
@RequestMapping("/api/schools/{school}/buildings/")
@Tag(name = "Buildings")
public class BuildingController {

    /** JOOQ Table for Building. */
    private static final com.eshabakhov.schoodule.tables.Building BUILDING =
        com.eshabakhov.schoodule.tables.Building.BUILDING;

    /** JOOQ DSL context for executing database queries. */
    private final DSLContext ctx;

    BuildingController(final DSLContext ctx) {
        this.ctx = ctx;
    }

    @PostMapping
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(
        summary = "Create building",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Building",
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
        description = "Building created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.building.simplebuilding+json",
            examples = {
                @ExampleObject(
                    name = "Simple building",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool building"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Building creation failed",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Field is required and cannot be empty",
                    summary = "Required field",
                    value = """
                        {
                            "message": "Field 'name' is required and cannot be empty",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
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
    public ResponseEntity<Building> create(
        @RequestHeader("version") final BuildingVersion version,
        @PathVariable final long school,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request building",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value =
                            """
                            {
                                "name": "Cool building"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (BuildingVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new BuildingRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            final Building building = new SlsPostgres(this.ctx)
                .school(school)
                .buildings()
                .create(name.asText());
            return ResponseEntity
                .created(
                    URI.create(
                        String.format("/api/schools/%d/buildings/%d", school, building.uid())
                    )
                )
                .body(new BdBase(building.uid(), building.name()));
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Fetch list of buildings")
    public ResponseEntity<PageableList<Building>> list(
        @PathVariable final long school,
        @RequestParam(
            name = "limit",
            required = false,
            defaultValue = "10"
        ) final int limit,
        @RequestParam(
            name = "offset",
            required = false,
            defaultValue = "1"
        ) final int offset,
        @RequestParam(
            value = "name_ct",
            required = false
        ) final String namect
    ) throws Exception {
        var condition = BuildingController.BUILDING.SCHOOL_ID.eq(school)
            .and(BuildingController.BUILDING.IS_DELETED.eq(false));
        if (namect != null && !namect.isBlank()) {
            condition = condition.and(
                BuildingController.BUILDING.NAME.likeIgnoreCase(String.format("%%%s%%", namect))
            );
        }
        return ResponseEntity
            .ok()
            .body(
                new SlsPostgres(this.ctx)
                    .school(school)
                    .buildings()
                    .buildings(condition, new PageRequest(limit, offset))
            );
    }

    @GetMapping("/{building}")
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(
        summary = "Fetch building",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Building",
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
        description = "Building fetched",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.building.simplebuilding+json",
            examples = {
                @ExampleObject(
                    name = "Simple building",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool building"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "Building not found",
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
    public Building get(
        @PathVariable final long school,
        @PathVariable final long building
    ) throws Exception {
        return new SlsPostgres(this.ctx)
            .school(school)
            .buildings()
            .building(building);
    }

    @PutMapping("/{building}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(
        summary = "Update building",
        parameters = {
            @Parameter(
                name = "version",
                in = ParameterIn.HEADER,
                description = "Version for representing Building",
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
        description = "Building updated",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.building.simplebuilding+json",
            examples = {
                @ExampleObject(
                    name = "Simple building",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool building"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "201",
        description = "Building created",
        content = @Content(
            mediaType = "application/com.eshabakhov.schoodule.school.building.simplebuilding+json",
            examples = {
                @ExampleObject(
                    name = "Simple building",
                    summary = "Simple",
                    value = """
                        {
                            "id": 1,
                            "name": "Cool building"
                        }"""
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "Building update failed",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "Field is required and cannot be empty",
                    summary = "Required field",
                    value = """
                        {
                            "message": "Field 'name' is required and cannot be empty",
                            "timestamp": "2026-01-22T08:24:38.037716369Z"
                        }"""
                )
            }
        )
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
    public ResponseEntity<Building> put(
        @RequestHeader("version") final BuildingVersion version,
        @PathVariable final long school,
        @PathVariable final long building,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Simple request building",
            content = @Content(
                examples = {
                    @ExampleObject(
                        name = "Simple",
                        value = """
                            {
                                "name": "Cool building"
                            }
                            """
                    )
                }
            )
        )
        @RequestBody final JsonNode request
    ) throws Exception {
        if (BuildingVersion.SIMPLE.equals(version)) {
            final JsonNode name = request.get("name");
            if (name == null || name.asText().isBlank()) {
                throw new BuildingRequiredFieldException(
                    "Field 'name' is required and cannot be empty"
                );
            }
            ResponseEntity<Building> response;
            try {
                response = ResponseEntity.ok()
                    .body(
                        new SlsPostgres(this.ctx)
                            .school(school)
                            .buildings()
                            .building(building)
                            .renamed(name.asText())
                    );
            } catch (final BdsPostgres.BuildingNotFoundException ex) {
                final var created = new SlsPostgres(this.ctx)
                    .school(school)
                    .buildings()
                    .create(name.asText());
                response = ResponseEntity
                    .created(
                        URI.create(
                            String.format("/api/schools/%d/buildings/%d", school, created.uid())
                        )
                    )
                    .body(created);
            }
            return response;
        } else {
            throw new VersionHeaderException(version.name());
        }
    }

    @DeleteMapping("/{building}")
    @PreAuthorize(
        """
        (hasAnyRole(
            'ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER'
        ))
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove building")
    public ResponseEntity<Void> delete(
        @PathVariable final long school,
        @PathVariable final long building
    ) throws Exception {
        new SlsPostgres(this.ctx)
            .school(school)
            .buildings()
            .remove(building);
        return ResponseEntity.noContent().build();
    }

    public static class BuildingRequiredFieldException extends Exception {
        public BuildingRequiredFieldException(final String message) {
            super(message);
        }
    }

    /** Building accept version. */
    public enum BuildingVersion {

        /** Version of simple building. */
        SIMPLE
    }
}
