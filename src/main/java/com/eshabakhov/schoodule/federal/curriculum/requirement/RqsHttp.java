/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.User;
import com.eshabakhov.schoodule.federal.curriculum.CrWithRequirements;
import com.eshabakhov.schoodule.federal.curriculum.CrsPostgres;
import com.eshabakhov.schoodule.federal.curriculum.CrsSingle;
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

/**
 * HTTP controller for federal curriculum requirements.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle NestedSwitchCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums/{curriculum}/requirements")
@Tag(name = "Federal Curriculum Requirements")
@PreAuthorize("hasRole('ADMIN')")
public class RqsHttp {

    /** JOOQ context. */
    private final DSLContext ctx;

    /**
     * New controller.
     *
     * @param ctx JOOQ context
     * @since 0.0.1
     */
    public RqsHttp(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Shows curriculum requirements.
     *
     * @param current Curriculum ID
     * @param filters Filtering
     * @param page Pagination
     * @param sorts Sorting
     * @return Printed representation
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(
        value = {"", "/fragment"},
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-requirements.v1+json",
            "application/vnd.schoodule.federal-curriculum-requirements.v1+xml"
        }
    )
    @Operation(summary = "List federal curriculum requirements")
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum requirements",
        content = @Content(schema = @Schema(type = "object"))
    )
    public Object selection(
        @PathVariable("curriculum") final long current,
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final Requirements selected = new RqsPostgres(this.ctx, current)
            .selection(filters, page, sorts);
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case String html when html.contains(MediaType.TEXT_HTML_VALUE)
                || html.contains(MediaType.ALL_VALUE) -> new ScWeb(
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
                            "xslt/federal-curriculum-requirements.xsl",
                            switch (
                                servlet.getRequest().getRequestURI()
                            ) {
                                case String uri when
                                    uri.endsWith("/fragment") ->
                                    "list-fragment";
                                case null -> "list";
                                default -> "list";
                            }
                        )
                    )
                ).with(
                    "curriculums",
                    new CrsSingle(
                        new CrsPostgres(this.ctx),
                        new CrWithRequirements(
                            new CrsPostgres(this.ctx).curriculum(current),
                            new RqsWeb(selected, filters, sorts)
                        )
                    )
                ).print();
            case "application/vnd.schoodule.federal-curriculum-requirements.v1+json" ->
                selected.print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-requirements.v1+xml" ->
                selected.print(new MdXml("requirements")).print();
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
            );
        };
    }

    /**
     * Shows one curriculum requirement.
     *
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @return Printed representation
     * @throws Exception If lookup fails
     */
    @ResponseBody
    @GetMapping(
        value = "/{requirement}",
        produces = {
            "application/vnd.schoodule.federal-curriculum-requirement-v1+json",
            "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
        }
    )
    @Operation(summary = "Get federal curriculum requirement")
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum requirement",
        content = @Content(schema = @Schema(type = "object"))
    )
    @ApiResponse(responseCode = "404", description = "Requirement not found")
    public Object requirement(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement
    ) throws Exception {
        return switch (
            ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest()
                .getHeader(HttpHeaders.ACCEPT)
        ) {
            case String json when
                "application/vnd.schoodule.federal-curriculum-requirement-v1+json"
                    .equals(json) -> new RqV1(
                        new RqsPostgres(this.ctx, current)
                            .requirement(requirement)
                    ).print(new MdJson()).print();
            case String xml when
                "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
                    .equals(xml) -> new RqV1(
                        new RqsPostgres(this.ctx, current)
                            .requirement(requirement)
                    ).print(new MdXml("requirement")).print();
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
            );
        };
    }

    /**
     * Creates a curriculum requirement.
     *
     * @param current Curriculum ID
     * @param grade Grade
     * @param subject Subject
     * @param hours Weekly hours
     * @param part Curriculum part
     * @return Printed representation
     * @throws Exception If creation fails
     */
    @ResponseBody
    @PostMapping(
        value = "/create",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+json",
            "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
        }
    )
    @Operation(summary = "Create federal curriculum requirement")
    @ApiResponse(
        responseCode = "201",
        description = "Federal curriculum requirement created"
    )
    @ApiResponse(
        responseCode = "302",
        description = "HTML client redirected to the requirements"
    )
    public Object create(
        @PathVariable("curriculum") final long current,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        final Requirement created = new RqsPostgres(this.ctx, current).create(
            grade, subject.trim(), hours, part
        );
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        final HttpServletResponse response = Objects.requireNonNull(
            servlet.getResponse()
        );
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case String html when html.contains(MediaType.TEXT_HTML_VALUE)
                || html.contains(MediaType.ALL_VALUE) -> {
                response.sendRedirect(
                    String.format(
                        "/federal/curriculums/%d/requirements", current
                    )
                );
                yield null;
            }
            case "application/vnd.schoodule.federal-curriculum-requirement-v1+json" -> {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(
                    HttpHeaders.LOCATION,
                    String.format(
                        "/federal/curriculums/%d/requirements/%d",
                        current,
                        created.uid()
                    )
                );
                yield new RqV1(created).print(new MdJson()).print();
            }
            case "application/vnd.schoodule.federal-curriculum-requirement-v1+xml" -> {
                response.setStatus(HttpStatus.CREATED.value());
                response.setHeader(
                    HttpHeaders.LOCATION,
                    String.format(
                        "/federal/curriculums/%d/requirements/%d",
                        current,
                        created.uid()
                    )
                );
                yield new RqV1(created).print(
                    new MdXml("requirement")
                ).print();
            }
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
            );
        };
    }

    /**
     * Updates a curriculum requirement.
     *
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @param grade Grade
     * @param subject Subject
     * @param hours Weekly hours
     * @param part Curriculum part
     * @return Printed representation
     * @throws Exception If update fails
     */
    @ResponseBody
    @PostMapping(
        value = "/{requirement}/edit",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-requirement-v1+json",
            "application/vnd.schoodule.federal-curriculum-requirement-v1+xml"
        }
    )
    @Operation(summary = "Update federal curriculum requirement")
    @ApiResponse(
        responseCode = "200",
        description = "Federal curriculum requirement updated"
    )
    @ApiResponse(
        responseCode = "302",
        description = "HTML client redirected to the requirements"
    )
    @ApiResponse(responseCode = "404", description = "Requirement not found")
    public Object revise(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        final Requirement revised = new RqsPostgres(this.ctx, current)
            .requirement(requirement)
            .regraded(grade)
            .resubjected(subject.trim())
            .reweekled(hours)
            .reparted(part);
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case String html when html.contains(MediaType.TEXT_HTML_VALUE)
                || html.contains(MediaType.ALL_VALUE) -> {
                Objects.requireNonNull(servlet.getResponse()).sendRedirect(
                    String.format(
                        "/federal/curriculums/%d/requirements", current
                    )
                );
                yield null;
            }
            case "application/vnd.schoodule.federal-curriculum-requirement-v1+json" ->
                new RqV1(revised).print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-requirement-v1+xml" ->
                new RqV1(revised).print(
                    new MdXml("requirement")
                ).print();
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
            );
        };
    }

    /**
     * Removes a curriculum requirement.
     *
     * @param current Curriculum ID
     * @param requirement Requirement ID
     * @throws Exception If removal fails
     */
    @DeleteMapping("/{requirement}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove federal curriculum requirement")
    @ApiResponse(
        responseCode = "204",
        description = "Federal curriculum requirement removed"
    )
    @ApiResponse(responseCode = "404", description = "Requirement not found")
    public void remove(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement
    ) throws Exception {
        new RqsPostgres(this.ctx, current).remove(requirement);
    }
}
