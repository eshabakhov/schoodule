/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.School;
import com.eshabakhov.surok.Schools;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.User;
import com.eshabakhov.surok.media.AcHttp;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.media.MdXml;
import com.eshabakhov.surok.media.MdXslt;
import com.eshabakhov.surok.media.SrWeb;
import com.eshabakhov.surok.media.XsClasspath;
import com.eshabakhov.surok.school.input.SchoolInputException;
import com.eshabakhov.surok.school.input.SlInputHttp;
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
 * Schools HTTP resource, for example GET /schools with Accept text/html.
 *
 * @since 0.0.1
 * @checkstyle NoJavadocForPrivateMethodsCheck (1000 lines)
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@RestController
@RequestMapping("/schools")
@Tag(name = "Schools")
public class SlsHttp {

    /** School collection. */
    private final Schools origin;

    /**
     * New school resource.
     *
     * @param ctx Database context
     */
    public SlsHttp(final DSLContext ctx) {
        this.origin = new SlsPostgres(ctx);
    }

    /**
     * Prints a collection, a fragment or a creation form.
     *
     * @param filters Applied filters
     * @param page Requested page
     * @param sorts Applied sorts
     * @param request HTTP request
     * @return Negotiated representation
     * @throws Exception If selection or printing fails
     */
    @GetMapping(
        value = {"", "/", "/fragment", "/create"},
        produces = {MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.schools.v1+json", "application/vnd.surok.schools.v1+xml"}
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List schools")
    public ResponseEntity<?> selection(
        final Filters filters, final Page page, final Sorts sorts,
        final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "schools.v1").type();
        final Schools selected = this.origin.selection(filters, page, sorts);
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
                    yield this.web(
                        request, "xslt/schools.xsl", view,
                        new SlsWeb(selected, filters, sorts)
                    ).print();
                }
                case "application/vnd.surok.schools.v1+json" ->
                    selected.print(new MdJson()).print();
                case "application/vnd.surok.schools.v1+xml" ->
                    selected.print(new MdXml("schools")).print();
                case null, default -> throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
                );
            }
        );
    }

    /**
     * Prints one school or its editing form.
     *
     * @param school School identifier
     * @param request HTTP request
     * @return Negotiated representation
     * @throws Exception If lookup or printing fails
     */
    @GetMapping(
        value = {"/{school}", "/{school}/edit"},
        produces = {MediaType.TEXT_HTML_VALUE,
            "application/vnd.surok.school-v1+json", "application/vnd.surok.school-v1+xml"}
    )
    @PreAuthorize("hasRole('ADMIN') or #school == authentication.principal.info().school()")
    @Operation(summary = "Get school")
    public ResponseEntity<?> school(
        @PathVariable final long school, final HttpServletRequest request
    ) throws Exception {
        final MediaType type = new AcHttp(request, "school-v1").type();
        final School selected = this.origin.school(school);
        return ResponseEntity.ok().contentType(new MediaType(type, StandardCharsets.UTF_8)).body(
            switch (type.toString()) {
                case MediaType.TEXT_HTML_VALUE -> {
                    final String view;
                    if (request.getRequestURI().endsWith("/edit")) {
                        view = "edit";
                    } else {
                        view = "details";
                    }
                    yield this.web(
                        request, "xslt/school.xsl", view, new SlsSingle(this.origin, selected)
                    ).with("school", new SlBrief(selected)).print();
                }
                case "application/vnd.surok.school-v1+json" ->
                    new SlV1(selected).print(new MdJson()).print();
                case "application/vnd.surok.school-v1+xml" ->
                    new SlV1(selected).print(new MdXml("school")).print();
                case null, default -> throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
                );
            }
        );
    }

    /**
     * Creates a school from form fields.
     *
     * @param parameters Form fields
     * @param request HTTP request
     * @return Created representation or redirect
     * @throws Exception If creation fails
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create school from form")
    public ResponseEntity<?> create(
        @RequestParam final MultiValueMap<String, String> parameters,
        final HttpServletRequest request
    ) throws Exception {
        return this.save(request, new SlInputHttp(parameters), 0L, true);
    }

    /**
     * Creates a school from a JSON document.
     *
     * @param json School fields
     * @param request HTTP request
     * @return Created representation
     * @throws Exception If creation fails
     */
    @PostMapping(
        value = {"", "/", "/create"},
        consumes = {MediaType.APPLICATION_JSON_VALUE, "application/vnd.surok.school-v1+json"}
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create school from JSON")
    public ResponseEntity<?> create(
        @RequestBody final JsonNode json, final HttpServletRequest request
    ) throws Exception {
        return this.save(request, new SlInputHttp(json), 0L, true);
    }

    /**
     * Revises a school from form fields.
     *
     * @param school School identifier
     * @param parameters Form fields
     * @param request HTTP request
     * @return Revised representation or redirect
     * @throws Exception If revision fails
     */
    @PostMapping(value = "/{school}/edit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Update school from form")
    public ResponseEntity<?> revise(
        @PathVariable final long school,
        @RequestParam final MultiValueMap<String, String> parameters,
        final HttpServletRequest request
    ) throws Exception {
        return this.save(request, new SlInputHttp(parameters), school, false);
    }

    /**
     * Revises an existing school from JSON without creating a replacement.
     *
     * @param school School identifier
     * @param json School fields
     * @param request HTTP request
     * @return Revised representation
     * @throws Exception If revision fails
     */
    @PutMapping(
        value = "/{school}",
        consumes = {MediaType.APPLICATION_JSON_VALUE, "application/vnd.surok.school-v1+json"}
    )
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Update school from JSON")
    public ResponseEntity<?> revise(
        @PathVariable final long school, @RequestBody final JsonNode json,
        final HttpServletRequest request
    ) throws Exception {
        return this.save(request, new SlInputHttp(json), school, false);
    }

    /**
     * Soft deletes a school.
     *
     * @param school School identifier
     * @return Empty response
     * @throws Exception If removal fails
     */
    @DeleteMapping("/{school}")
    @PreAuthorize(
        """
        hasAnyRole('ADMIN', 'DIRECTOR', 'BASIC_MAKER', 'ADVANCED_MAKER', 'PRO_MAKER')
        and (hasRole('ADMIN') or #school == authentication.principal.info().school())
        """
    )
    @Operation(summary = "Remove school")
    public ResponseEntity<Void> remove(@PathVariable final long school) throws Exception {
        this.origin.remove(school);
        return ResponseEntity.noContent().build();
    }

    /**
     * Negotiates the response before applying a validated mutation.
     *
     * @param request HTTP request
     * @param input School input
     * @param id School identifier
     * @param creation Whether to create a school
     * @return Mutation response
     * @throws Exception If validation, lookup or printing fails
     */
    private ResponseEntity<?> save(
        final HttpServletRequest request, final SchoolInput input,
        final long id, final boolean creation
    ) throws Exception {
        final MediaType type = new AcHttp(request, "school-v1").type();
        ResponseEntity<?> response;
        try {
            final School selected;
            final HttpStatus status;
            if (creation) {
                selected = this.origin.create(input);
                status = HttpStatus.CREATED;
            } else {
                selected = this.origin.revise(id, input);
                status = HttpStatus.OK;
            }
            final URI location = URI.create(String.format("/schools/%d", selected.uid()));
            final ResponseEntity.BodyBuilder builder = ResponseEntity.status(status)
                .contentType(new MediaType(type, StandardCharsets.UTF_8));
            if (HttpStatus.CREATED.equals(status)) {
                builder.location(location);
            }
            response = switch (type.toString()) {
                case MediaType.TEXT_HTML_VALUE -> ResponseEntity.status(HttpStatus.FOUND)
                    .location(location).build();
                case "application/vnd.surok.school-v1+json" ->
                    builder.body(new SlV1(selected).print(new MdJson()).print());
                case "application/vnd.surok.school-v1+xml" ->
                    builder.body(new SlV1(selected).print(new MdXml("school")).print());
                case null, default -> throw new ResponseStatusException(
                    HttpStatus.NOT_ACCEPTABLE, "Unsupported Accept header"
                );
            };
        } catch (final SchoolInputException exception) {
            if (!MediaType.TEXT_HTML.equals(type)) {
                throw exception;
            }
            final String location;
            if (creation) {
                location = "/schools/create?error=empty";
            } else {
                location = String.format("/schools/%d/edit?error=empty", id);
            }
            response = ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(location)).build();
        }
        return response;
    }

    /**
     * Builds shared browser data and renders the requested stylesheet.
     *
     * @param request HTTP request
     * @param stylesheet Stylesheet path
     * @param view Selected view
     * @param schools Printed school collection
     * @return Browser medium
     * @throws Exception If printing fails
     */
    // @checkstyle NonStaticMethodCheck (2 lines)
    private Media<String> web(
        final HttpServletRequest request, final String stylesheet,
        final String view, final Schools schools
    ) throws Exception {
        return new SrWeb(
            new UrBrief(
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            ), schools,
            new CsToken((CsrfToken) request.getAttribute(CsrfToken.class.getName()))
        ).print(new MdXslt(new MdXml("surok"), new XsClasspath(stylesheet, view)))
            .with("error", request.getParameter("error") != null);
    }
}
