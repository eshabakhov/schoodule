/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.User;
import com.eshabakhov.schoodule.media.MdJson;
import com.eshabakhov.schoodule.media.MdXml;
import com.eshabakhov.schoodule.media.MdXslt;
import com.eshabakhov.schoodule.media.ScWeb;
import com.eshabakhov.schoodule.media.XsClasspath;
import com.eshabakhov.schoodule.school.SlsBrief;
import com.eshabakhov.schoodule.school.SlsPostgres;
import com.eshabakhov.schoodule.security.CsToken;
import com.eshabakhov.schoodule.user.UrBrief;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

/**
 * HTTP controller for federal curriculums.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums")
@Tag(name = "Federal Curriculums")
@PreAuthorize("hasRole('ADMIN')")
public class CrsHttp {

    /** JOOQ context. */
    private final DSLContext ctx;

    /** Origin curriculums. */
    private final Curriculums origin;

    /**
     * New controller.
     *
     * @param ctx JOOQ context
     * @since 0.0.1
     */
    public CrsHttp(final DSLContext ctx) {
        this.ctx = ctx;
        this.origin = new CrsPostgres(ctx);
    }

    /**
     * Creates a federal curriculum.
     *
     * @param title Title
     * @param level Education level
     * @param week Study week
     * @param version Version
     * @param year Academic year
     * @param description Description
     * @return Printed representation
     * @throws Exception If creation fails
     */
    @ResponseBody
    @PostMapping(
        value = "/create",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-v1+json",
            "application/vnd.schoodule.federal-curriculum-v2+json",
            "application/vnd.schoodule.federal-curriculum-v1+xml",
            "application/vnd.schoodule.federal-curriculum-v2+xml"
        }
    )
    @Operation(summary = "Create federal curriculum")
    @ApiResponse(
        responseCode = "201",
        description = "Federal curriculum created"
    )
    @ApiResponse(
        responseCode = "302",
        description = "HTML client redirected to the created curriculum"
    )
    public Object create(
        @RequestParam final String title,
        @RequestParam(name = "level") final Curriculum.Level level,
        @RequestParam(name = "week") final Curriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        final Curriculum created = this.origin.create(
            title.trim(),
            level,
            week,
            version.trim(),
            year.trim(),
            Optional.ofNullable(description).map(String::trim).orElse(null)
        );
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        final HttpServletResponse response = Objects.requireNonNull(
            servlet.getResponse()
        );
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String html when
                html.contains(MediaType.TEXT_HTML_VALUE)
                    || html.contains(MediaType.ALL_VALUE) -> {
                response.sendRedirect(
                    String.format(
                        "/federal/curriculums/%d", created.uid()
                    )
                );
                yield null;
            }
            case "application/vnd.schoodule.federal-curriculum-v1+json" -> {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(
                    HttpHeaders.LOCATION,
                    String.format(
                        "/federal/curriculums/%d", created.uid()
                    )
                );
                yield new CrV1(created).print(new MdJson()).print();
            }
            case "application/vnd.schoodule.federal-curriculum-v2+json" -> {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(
                    HttpHeaders.LOCATION,
                    String.format(
                        "/federal/curriculums/%d", created.uid()
                    )
                );
                yield new CrV2(created).print(new MdJson()).print();
            }
            case "application/vnd.schoodule.federal-curriculum-v1+xml" -> {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(
                    HttpHeaders.LOCATION,
                    String.format(
                        "/federal/curriculums/%d", created.uid()
                    )
                );
                yield new CrV1(created).print(
                    new MdXml("curriculum")
                ).print();
            }
            case "application/vnd.schoodule.federal-curriculum-v2+xml" -> {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(
                    HttpHeaders.LOCATION,
                    String.format(
                        "/federal/curriculums/%d", created.uid()
                    )
                );
                yield new CrV2(created).print(
                    new MdXml("curriculum")
                ).print();
            }
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
            );
        };
    }

    /**
     * Shows a federal curriculum.
     *
     * @param curriculum Curriculum ID
     * @return Printed representation
     * @throws Exception If lookup fails
     */
    @ResponseBody
    @GetMapping(
        value = {"/{curriculum}", "/{curriculum}/edit"},
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-v1+json",
            "application/vnd.schoodule.federal-curriculum-v2+json",
            "application/vnd.schoodule.federal-curriculum-v1+xml",
            "application/vnd.schoodule.federal-curriculum-v2+xml"
        }
    )
    @Operation(summary = "Get federal curriculum")
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum",
        content = @Content(schema = @Schema(type = "object"))
    )
    @ApiResponse(responseCode = "404", description = "Curriculum not found")
    public Object curriculum(@PathVariable final long curriculum)
        throws Exception {
        final Curriculum selected = this.origin.curriculum(curriculum);
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        final String view;
        if (
            Objects.toString(
                servlet.getRequest().getAttribute(
                    HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE
                ),
                ""
            ).endsWith("/{curriculum}/edit")
        ) {
            view = "edit";
        } else {
            view = "details";
        }
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String html when
                html.contains(MediaType.TEXT_HTML_VALUE)
                    || html.contains(MediaType.ALL_VALUE) -> {
                yield new ScWeb(
                    new UrBrief(
                        (User) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal()
                    ),
                    new SlsBrief(new SlsPostgres(this.ctx)),
                    new CsToken(
                        (CsrfToken) servlet.getRequest().getAttribute(
                            CsrfToken.class.getName()
                        )
                    )
                ).print(
                    new MdXslt(
                        new MdXml("schoodule"),
                        new XsClasspath(
                            "xslt/federal-curriculums.xsl",
                            view
                        )
                    )
                ).with(
                    "curriculums", new CrsSingle(this.origin, selected)
                ).print();
            }
            case "application/vnd.schoodule.federal-curriculum-v1+json" ->
                new CrV1(selected).print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-v2+json" ->
                new CrV2(selected).print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-v1+xml" ->
                new CrV1(selected).print(
                    new MdXml("curriculum")
                ).print();
            case "application/vnd.schoodule.federal-curriculum-v2+xml" ->
                new CrV2(selected).print(
                    new MdXml("curriculum")
                ).print();
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
            );
        };
    }

    /**
     * Updates a federal curriculum.
     *
     * @param curriculum Curriculum ID
     * @param title Title
     * @param level Education level
     * @param week Study week
     * @param version Version
     * @param year Academic year
     * @param description Description
     * @return Printed representation
     * @throws Exception If update fails
     */
    @ResponseBody
    @PostMapping(
        value = "/{curriculum}/edit",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-v1+json",
            "application/vnd.schoodule.federal-curriculum-v2+json",
            "application/vnd.schoodule.federal-curriculum-v1+xml",
            "application/vnd.schoodule.federal-curriculum-v2+xml"
        }
    )
    @Operation(summary = "Update federal curriculum")
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum updated"
    )
    @ApiResponse(
        responseCode = "302",
        description = "HTML client redirected to the updated curriculum"
    )
    @ApiResponse(responseCode = "404", description = "Curriculum not found")
    public Object revise(
        @PathVariable final long curriculum,
        @RequestParam final String title,
        @RequestParam(name = "level") final Curriculum.Level level,
        @RequestParam(name = "week") final Curriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        final Curriculum revised = this.origin.revise(
            curriculum,
            title.trim(),
            level,
            week,
            version.trim(),
            year.trim(),
            Optional.ofNullable(description).map(String::trim).orElse(null)
        );
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String html when
                html.contains(MediaType.TEXT_HTML_VALUE)
                    || html.contains(MediaType.ALL_VALUE) -> {
                Objects.requireNonNull(servlet.getResponse()).sendRedirect(
                    String.format(
                        "/federal/curriculums/%d", revised.uid()
                    )
                );
                yield null;
            }
            case "application/vnd.schoodule.federal-curriculum-v1+json" ->
                new CrV1(revised).print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-v2+json" ->
                new CrV2(revised).print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-v1+xml" ->
                new CrV1(revised).print(
                    new MdXml("curriculum")
                ).print();
            case "application/vnd.schoodule.federal-curriculum-v2+xml" ->
                new CrV2(revised).print(
                    new MdXml("curriculum")
                ).print();
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
            );
        };
    }

    /**
     * Shows federal curriculums.
     *
     * @param filters Filtering
     * @param page Pagination
     * @param sorts Sorting
     * @return Printed representation
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(
        value = {"", "/fragment", "/create"},
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculums.v1+json",
            "application/vnd.schoodule.federal-curriculums.v1+xml"
        }
    )
    @Operation(summary = "List federal curriculums")
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculums",
        content = @Content(schema = @Schema(type = "object"))
    )
    public Object selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Curriculums selected = this.origin.selection(
            filters, page, sorts
        );
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        final String pattern = Objects.toString(
            servlet.getRequest().getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE
            ),
            ""
        );
        final String view;
        if (pattern.endsWith("/fragment")) {
            view = "list-fragment";
        } else if (pattern.endsWith("/create")) {
            view = "create";
        } else {
            view = "list";
        }
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String html when
                html.contains(MediaType.TEXT_HTML_VALUE)
                    || html.contains(MediaType.ALL_VALUE) -> {
                yield new ScWeb(
                    new UrBrief(
                        (User) SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal()
                    ),
                    new SlsBrief(new SlsPostgres(this.ctx)),
                    new CsToken(
                        (CsrfToken) servlet.getRequest().getAttribute(
                            CsrfToken.class.getName()
                        )
                    )
                ).print(
                    new MdXslt(
                        new MdXml("schoodule"),
                        new XsClasspath(
                            "xslt/federal-curriculums.xsl",
                            view
                        )
                    )
                ).with(
                    "curriculums", new CrsWeb(selected, filters)
                ).print();
            }
            case "application/vnd.schoodule.federal-curriculums.v1+json" ->
                selected.print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculums.v1+xml" ->
                selected.print(new MdXml("curriculums")).print();
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
            );
        };
    }

    /**
     * Removes a federal curriculum.
     *
     * @param id Curriculum ID
     * @throws Exception If removal fails
     */
    @DeleteMapping("/{curriculum}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove federal curriculum")
    @ApiResponse(
        responseCode = "204",
        description = "Federal curriculum removed"
    )
    @ApiResponse(responseCode = "404", description = "Curriculum not found")
    public void remove(
        @PathVariable(name = "curriculum") final long id
    ) throws Exception {
        this.origin.remove(id);
    }
}
