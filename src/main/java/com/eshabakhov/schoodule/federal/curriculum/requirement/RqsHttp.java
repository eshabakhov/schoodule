/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.federal.curriculum.CrsPostgres;
import com.eshabakhov.schoodule.media.MdJson;
import com.eshabakhov.schoodule.media.MdThymeleaf;
import com.eshabakhov.schoodule.media.MdXml;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.jooq.DSLContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

/**
 * HTTP controller for federal curriculum requirements.
 *
 * @since 0.0.1
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums/{curriculum}/requirements")
@PreAuthorize("hasRole('ADMIN')")
public class RqsHttp {

    /** JOOQ context. */
    private final DSLContext ctx;

    /** Thymeleaf resolver. */
    private final ThymeleafViewResolver views;

    /**
     * New controller.
     *
     * @param ctx JOOQ context
     * @param views Thymeleaf resolver
     * @since 0.0.1
     */
    public RqsHttp(final DSLContext ctx, final ThymeleafViewResolver views) {
        this.ctx = ctx;
        this.views = views;
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
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum-requirements.v1+json",
            "application/vnd.schoodule.federal-curriculum-requirements.v1+xml"
        }
    )
    public Object list(
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
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String html when
                html.contains(MediaType.TEXT_HTML_VALUE)
                    || html.contains(MediaType.ALL_VALUE) -> {
                final ModelAndView view = selected.print(
                    new CrsPostgres(this.ctx).curriculum(current).print(
                        new MdThymeleaf(
                            "federal-curriculums/requirements", ""
                        )
                    )
                ).title("Требования: %s").attributes(
                    RqsHttp.requirementsModel(current, sorts)
                ).print();
                Objects.requireNonNull(
                    this.views.resolveViewName(
                        view.getViewName(), servlet.getRequest().getLocale()
                    )
                ).render(
                    view.getModel(),
                    servlet.getRequest(),
                    Objects.requireNonNull(servlet.getResponse())
                );
                yield null;
            }
            case "application/vnd.schoodule.federal-curriculum-requirements.v1+json" ->
                selected.print(new MdJson()).print();
            case "application/vnd.schoodule.federal-curriculum-requirements.v1+xml" ->
                selected.print(new MdXml()).print();
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
            );
        };
    }

    /**
     * Shows the requirements fragment.
     *
     * @param current Curriculum ID
     * @param filters Filtering
     * @param page Pagination
     * @param sorts Sorting
     * @return Printed representation
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public Object fragment(
        @PathVariable("curriculum") final long current,
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final ServletRequestAttributes servlet = (ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes();
        return switch (servlet.getRequest().getHeader(HttpHeaders.ACCEPT)) {
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String html when
                html.contains(MediaType.TEXT_HTML_VALUE)
                    || html.contains(MediaType.ALL_VALUE) -> {
                final ModelAndView view = new RqsPostgres(this.ctx, current)
                    .selection(filters, page, sorts).print(
                        new MdThymeleaf(
                            "federal-curriculums/requirements :: requirements-results",
                            ""
                        )
                    ).attributes(
                        RqsHttp.requirementsModel(current, sorts)
                    ).print();
                Objects.requireNonNull(
                    this.views.resolveViewName(
                        view.getViewName(), servlet.getRequest().getLocale()
                    )
                ).render(
                    view.getModel(),
                    servlet.getRequest(),
                    Objects.requireNonNull(servlet.getResponse())
                );
                yield null;
            }
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
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
            "application/vnd.schoodule.federal-curriculum-requirement.simple.v1+json",
            "application/vnd.schoodule.federal-curriculum-requirement.simple.v1+xml"
        }
    )
    public Object details(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement
    ) throws Exception {
        return switch (
            ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest()
                .getHeader(HttpHeaders.ACCEPT)
        ) {
            case null -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Accept header is required"
            );
            case String json when
                "application/vnd.schoodule.federal-curriculum-requirement.simple.v1+json"
                    .equals(json) -> new RqSimple(
                        new RqsPostgres(this.ctx, current)
                            .requirement(requirement)
                    ).print(new MdJson()).print();
            case String xml when
                "application/vnd.schoodule.federal-curriculum-requirement.simple.v1+xml"
                    .equals(xml) -> new RqSimple(
                        new RqsPostgres(this.ctx, current)
                            .requirement(requirement)
                    ).print(new MdXml()).print();
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
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
    @PostMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    public Object created(
        @PathVariable("curriculum") final long current,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        new RqsPostgres(this.ctx, current).create(
            grade, subject.trim(), hours, part
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
                        "/federal/curriculums/%d/requirements", current
                    )
                );
                yield null;
            }
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
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
        produces = MediaType.TEXT_HTML_VALUE
    )
    public Object edited(
        @PathVariable("curriculum") final long current,
        @PathVariable final long requirement,
        @RequestParam final Integer grade,
        @RequestParam(name = "subjectName") final String subject,
        @RequestParam(name = "weeklyHours") final Integer hours,
        @RequestParam(name = "partType") final Requirement.PartType part
    ) throws Exception {
        new RqsPostgres(this.ctx, current).requirement(requirement)
            .regraded(grade)
            .resubjected(subject.trim())
            .reweekled(hours)
            .reparted(part);
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
                        "/federal/curriculums/%d/requirements", current
                    )
                );
                yield null;
            }
            default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE,
                "Unsupported Accept header"
            );
        };
    }

    private static Map<String, Object> requirementsModel(
        final long curriculum,
        final Sorts sorts
    ) {
        String grade = "";
        String subject = "";
        String hours = "";
        String part = "";
        for (final Sort item : sorts.sorts()) {
            final String direction;
            if (Sort.Direction.NONE.equals(item.direction())) {
                direction = "";
            } else {
                direction = item.direction().name().toLowerCase(Locale.ROOT);
            }
            if ("grade".equals(item.name())) {
                grade = direction;
            } else if ("subject".equals(item.name())) {
                subject = direction;
            } else if ("hours".equals(item.name())) {
                hours = direction;
            } else if ("part".equals(item.name())) {
                part = direction;
            }
        }
        return Map.ofEntries(
            Map.entry("id", curriculum),
            Map.entry("partTypes", Requirement.PartType.values()),
            Map.entry("gradeSort", grade),
            Map.entry("subjectSort", subject),
            Map.entry("hoursSort", hours),
            Map.entry("partSort", part)
        );
    }
}
