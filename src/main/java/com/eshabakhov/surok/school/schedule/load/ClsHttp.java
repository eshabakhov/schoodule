/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.schedule.load;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.School;
import com.eshabakhov.surok.User;
import com.eshabakhov.surok.media.AcHttp;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.media.MdXml;
import com.eshabakhov.surok.media.MdXslt;
import com.eshabakhov.surok.media.SrWeb;
import com.eshabakhov.surok.media.XsClasspath;
import com.eshabakhov.surok.school.SlBrief;
import com.eshabakhov.surok.school.SlsBrief;
import com.eshabakhov.surok.school.SlsPostgres;
import com.eshabakhov.surok.school.schedule.Schedule;
import com.eshabakhov.surok.school.schedule.SdBrief;
import com.eshabakhov.surok.school.schedule.load.input.ClInputHttp;
import com.eshabakhov.surok.school.schedule.load.input.LoadInputException;
import com.eshabakhov.surok.security.CsToken;
import com.eshabakhov.surok.user.UrBrief;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unified teaching load HTTP resource; Accept selects HTML or versioned JSON/XML.
 * For example, request application/vnd.surok.class-loads.v1+json for the collection.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@RestController
@RequestMapping("/schools/{school}/schedules/{schedule}/loads")
@PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
@Tag(name = "Teaching loads")
public class ClsHttp {

    /** Database context. */
    private final DSLContext ctx;

    /**
     * New resource.
     *
     * @param ctx Database context
     */
    public ClsHttp(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Shows a page of loads, its fragment or the creation form.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param page Pagination
     * @param request HTTP request
     * @return Negotiated representation
     * @throws Exception If lookup or printing fails
     */
    @GetMapping(
        value = {"", "/", "/fragment", "/create"},
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.class-loads.v1+json",
            "application/vnd.surok.class-loads.v1+xml"
        }
    )
    @Operation(summary = "List teaching loads")
    public ResponseEntity<?> selection(
        @PathVariable final long school,
        @PathVariable final long schedule,
        final Page page,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "class-loads.v1").type();
        final School sch = new SlsPostgres(this.ctx).school(school);
        final Schedule sched = sch.schedules().schedule(schedule);
        final ClassLoads selected = sched.loads().selection(page);
        return ResponseEntity.ok().contentType(new MediaType(type, StandardCharsets.UTF_8)).body(
            switch (type.toString()) {
                case MediaType.TEXT_HTML_VALUE -> {
                    final String view;
                    if (request.getRequestURI().endsWith("/fragment")) {
                        view = "list-fragment";
                    } else if (request.getRequestURI().endsWith("/create")) {
                        view = "create";
                    } else {
                        view = "list";
                    }
                    yield this.web(request, sch, sched, view).with("loads", selected).print();
                }
                case "application/vnd.surok.class-loads.v1+json" ->
                    selected.print(new MdJson()).print();
                case "application/vnd.surok.class-loads.v1+xml" ->
                    selected.print(new MdXml("loads")).print();
                case null, default -> throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
                );
            }
        );
    }

    /**
     * Shows one load or its editing form.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Entity identifier
     * @param request HTTP request
     * @return Negotiated representation
     * @throws Exception If lookup or printing fails
     */
    @GetMapping(
        value = {"/{load}", "/{load}/edit"},
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.class-load-v1+json",
            "application/vnd.surok.class-load-v1+xml"
        }
    )
    @Operation(summary = "Get teaching load")
    public ResponseEntity<?> load(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "class-load-v1").type();
        final School sch = new SlsPostgres(this.ctx).school(school);
        final Schedule sched = sch.schedules().schedule(schedule);
        final ClassLoads origin = sched.loads();
        final ClassLoad selected = origin.load(load);
        return ResponseEntity.ok().contentType(new MediaType(type, StandardCharsets.UTF_8)).body(
            switch (type.toString()) {
                case MediaType.TEXT_HTML_VALUE -> {
                    final String view;
                    if (request.getRequestURI().endsWith("/edit")) {
                        view = "edit";
                    } else {
                        view = "details";
                    }
                    yield this.web(request, sch, sched, view).with(
                        "loads", new ClsSingle(origin, selected)
                    ).print();
                }
                case "application/vnd.surok.class-load-v1+json" ->
                    new ClV1(selected).print(new MdJson()).print();
                case "application/vnd.surok.class-load-v1+xml" ->
                    new ClV1(selected).print(new MdXml("load")).print();
                case null, default -> throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
                );
            }
        );
    }

    /**
     * Creates a load from form fields.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param parameters Form fields
     * @param request HTTP request
     * @return Created representation or redirect
     * @throws Exception If creation fails
     */
    @PostMapping(
        value = "/create",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.class-load-v1+json",
            "application/vnd.surok.class-load-v1+xml"
        }
    )
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Create teaching load from form")
    public ResponseEntity<?> create(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestParam final MultiValueMap<String, String> parameters,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "class-load-v1").type();
        ResponseEntity<?> response;
        try {
            response = ClsHttp.result(
                type,
                new SlsPostgres(this.ctx).school(school).schedules()
                    .schedule(schedule).loads()
                    .create(new ClInputHttp(parameters)), HttpStatus.CREATED,
                String.format("/schools/%d/schedules/%d/loads", school, schedule),
                String.format("/schools/%d/schedules/%d/loads", school, schedule)
            );
        } catch (final LoadInputException | LoadAlreadyExistsException exception) {
            response = ClsHttp.rejected(type, request, exception);
        }
        return response;
    }

    /**
     * Creates a load from a JSON document.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param json Input document
     * @param request HTTP request
     * @return Created representation
     * @throws Exception If creation fails
     */
    @PostMapping(
        value = {"", "/", "/create"},
        consumes = "application/vnd.surok.class-load-v1+json",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.class-load-v1+json",
            "application/vnd.surok.class-load-v1+xml"
        }
    )
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Create teaching load from JSON")
    public ResponseEntity<?> create(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @RequestBody final JsonNode json,
        final HttpServletRequest request
    ) throws Exception {
        return ClsHttp.result(
            new AcHttp(request, "class-load-v1").type(),
            new SlsPostgres(this.ctx).school(school).schedules()
                .schedule(schedule).loads()
                .create(new ClInputHttp(json)), HttpStatus.CREATED,
            String.format("/schools/%d/schedules/%d/loads", school, schedule),
            String.format("/schools/%d/schedules/%d/loads", school, schedule)
        );
    }

    /**
     * Revises a load from form fields.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Entity identifier
     * @param parameters Form fields
     * @param request HTTP request
     * @return Revised representation or redirect
     * @throws Exception If revision fails
     */
    @PostMapping(
        value = "/{load}/edit",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.class-load-v1+json",
            "application/vnd.surok.class-load-v1+xml"
        }
    )
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Update teaching load from form")
    public ResponseEntity<?> revise(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load,
        @RequestParam final MultiValueMap<String, String> parameters,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "class-load-v1").type();
        ResponseEntity<?> response;
        try {
            final ClassLoad revised = new SlsPostgres(this.ctx).school(school).schedules()
                .schedule(schedule).loads()
                .revise(load, new ClInputHttp(parameters));
            response = ClsHttp.result(
                type, revised, HttpStatus.OK,
                String.format("/schools/%d/schedules/%d/loads", school, schedule),
                String.format(
                    "/schools/%d/schedules/%d/loads/%d", school, schedule, revised.uid()
                )
            );
        } catch (final LoadInputException | LoadAlreadyExistsException exception) {
            response = ClsHttp.rejected(type, request, exception);
        }
        return response;
    }

    /**
     * Revises a load from a JSON document.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Entity identifier
     * @param json Input document
     * @param request HTTP request
     * @return Revised representation or redirect
     * @throws Exception If revision fails
     */
    @PutMapping(
        value = "/{load}",
        consumes = "application/vnd.surok.class-load-v1+json",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.class-load-v1+json",
            "application/vnd.surok.class-load-v1+xml"
        }
    )
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Update teaching load from JSON")
    public ResponseEntity<?> revise(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load,
        @RequestBody final JsonNode json,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "class-load-v1").type();
        final ClassLoad revised = new SlsPostgres(this.ctx).school(school).schedules()
            .schedule(schedule).loads()
            .revise(load, new ClInputHttp(json));
        return ClsHttp.result(
            type, revised, HttpStatus.OK,
            String.format("/schools/%d/schedules/%d/loads", school, schedule),
            String.format("/schools/%d/schedules/%d/loads/%d", school, schedule, revised.uid())
        );
    }

    /**
     * Removes a load.
     *
     * @param school School identifier
     * @param schedule Schedule identifier
     * @param load Entity identifier
     * @return Empty response
     * @throws Exception If removal fails
     */
    @DeleteMapping("/{load}")
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'DEPUTY_DIRECTOR',
            'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove teaching load")
    public ResponseEntity<Void> remove(
        @PathVariable final long school,
        @PathVariable final long schedule,
        @PathVariable final long load
    ) throws Exception {
        new SlsPostgres(this.ctx).school(school).schedules()
            .schedule(schedule).loads().remove(load);
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<?> result(
        final MediaType type,
        final ClassLoad selected,
        final HttpStatus status,
        final String base,
        final String redirect
    ) throws Exception {
        return switch (type.toString()) {
            case MediaType.TEXT_HTML_VALUE -> ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect)).build();
            case "application/vnd.surok.class-load-v1+json" ->
                ResponseEntity.status(status)
                    .location(URI.create(String.format("%s/%d", base, selected.uid())))
                    .contentType(new MediaType(type, StandardCharsets.UTF_8))
                    .body(new ClV1(selected).print(new MdJson()).print());
            case "application/vnd.surok.class-load-v1+xml" ->
                ResponseEntity.status(status)
                    .location(URI.create(String.format("%s/%d", base, selected.uid())))
                    .contentType(new MediaType(type, StandardCharsets.UTF_8))
                    .body(new ClV1(selected).print(new MdXml("load")).print());
            case null, default -> throw new ResponseStatusException(
                HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
            );
        };
    }

    private static ResponseEntity<?> rejected(
        final MediaType type,
        final HttpServletRequest request,
        final Exception exception
    ) throws Exception {
        if (!MediaType.TEXT_HTML.equals(type)) {
            throw exception;
        }
        String error = "input";
        if (exception instanceof LoadAlreadyExistsException) {
            error = "duplicate";
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(
            URI.create(String.format("%s?error=%s", request.getRequestURI(), error))
        ).build();
    }

    private Media<String> web(
        final HttpServletRequest request,
        final School school,
        final Schedule schedule,
        final String view
    ) throws Exception {
        return new SrWeb(
            new UrBrief(
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            ),
            new SlsBrief(new SlsPostgres(this.ctx)),
            new CsToken((CsrfToken) request.getAttribute(CsrfToken.class.getName()))
        ).print(
            new MdXslt(new MdXml("surok"), new XsClasspath("xslt/class-loads.xsl", view))
        ).with("school", new SlBrief(school))
            .with("schedule", new SdBrief(schedule))
            .with("classes", school.schoolClasses())
            .with("subjects", school.subjects())
            .with("error", request.getParameter("error"));
    }
}
