/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Header;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.media.MdThymeleaf;
import java.util.Map;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * HTTP and domain implementation of {@link Curriculums}.
 *
 * @since 0.0.1
 * @checkstyle DesignForExtensionCheck (1000 lines)
 * @checkstyle NonStaticMethodCheck (1000 lines)
 * @checkstyle ParameterNumberCheck (1000 lines)
 */
@Controller
@RequestMapping("/federal/curriculums")
@PreAuthorize("hasRole('ADMIN')")
public class CrsHttp implements Curriculums {

    /**
     * Origin curriculums.
     */
    private final Curriculums origin;

    /**
     * New controller.
     *
     * @param ctx JOOQ context
     * @since 0.0.1
     */
    public CrsHttp(final DSLContext ctx) {
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
     * Shows federal curriculums.
     *
     * @param page Pagination
     * @param sorts Sorting
     * @param filters Filtering
     * @param header Representation header
     * @return Printed curriculums
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculums.v1+json",
            "application/vnd.schoodule.federal-curriculums.v1+xml"
        }
    )
    public Object list(
        final Page page,
        final Sorts sorts,
        final Filters filters,
        final Header<?> header
    ) throws Exception {
        return this.selection(filters, page, sorts)
            .print(header.media("federal-curriculums/list", ""))
            .print();
    }

    /**
     * Shows the federal curriculums fragment.
     *
     * @param filters Filtering
     * @param page Pagination
     * @param sorts Sorting
     * @param header Representation header
     * @return Printed fragment
     * @throws Exception If selection fails
     */
    @ResponseBody
    @GetMapping(value = "/fragment", produces = MediaType.TEXT_HTML_VALUE)
    public Object fragment(
        final Filters filters,
        final Page page,
        final Sorts sorts,
        final Header<?> header
    ) throws Exception {
        return this.selection(filters, page, sorts).print(
            header.media(
                "federal-curriculums/list :: curriculums-grid",
                ""
            )
        ).print();
    }

    /**
     * Shows the curriculum creation form.
     *
     * @return Creation form
     */
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView createForm() {
        return new MdThymeleaf("federal-curriculums/create", "").attributes(
            Map.of(
                "pageTitle", "Новый федеральный учебный план",
                "levels", Curriculum.Level.values(),
                "studyWeeks", Curriculum.Week.values()
            )
        ).print();
    }

    /**
     * Creates a federal curriculum from form values.
     *
     * @param title Curriculum title
     * @param level Education level
     * @param week Study week
     * @param version Curriculum version
     * @param year Academic year
     * @param description Description
     * @return Redirect to curriculum
     * @throws Exception If creation fails
     */
    @PostMapping("/create")
    public String created(
        @RequestParam final String title,
        @RequestParam(name = "level") final Curriculum.Level level,
        @RequestParam(name = "week") final Curriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        return String.format(
            "redirect:/federal/curriculums/%d",
            this.create(
                title.trim(),
                level,
                week,
                version.trim(),
                year.trim(),
                Optional.ofNullable(description).map(String::trim).orElse(null)
            ).uid()
        );
    }

    /**
     * Shows curriculum details.
     *
     * @param curriculum Curriculum ID
     * @param header Representation header
     * @return Printed curriculum
     * @throws Exception If lookup fails
     */
    @ResponseBody
    @GetMapping(
        value = "/{curriculum}",
        produces = {
            MediaType.TEXT_HTML_VALUE,
            "application/vnd.schoodule.federal-curriculum.simple.v1+json",
            "application/vnd.schoodule.federal-curriculum.full.v1+json",
            "application/vnd.schoodule.federal-curriculum.simple.v1+xml",
            "application/vnd.schoodule.federal-curriculum.full.v1+xml"
        }
    )
    public Object details(
        @PathVariable final long curriculum,
        final Header<?> header
    ) throws Exception {
        return this.curriculum(curriculum).print(
            header.media(
                "federal-curriculums/details",
                "curriculum"
            )
        ).print();
    }

    /**
     * Shows the curriculum editing form.
     *
     * @param curriculum Curriculum ID
     * @return Editing form
     * @throws Exception If lookup fails
     */
    @GetMapping(value = "/{curriculum}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView editForm(@PathVariable final long curriculum) throws Exception {
        return this.curriculum(curriculum).print(
            new MdThymeleaf(
                "federal-curriculums/edit",
                ""
            )
        ).attributes(
            Map.of(
                "pageTitle", "Редактировать федеральный учебный план",
                "levels", Curriculum.Level.values(),
                "studyWeeks", Curriculum.Week.values()
            )
        ).print();
    }

    /**
     * Updates a federal curriculum from form values.
     *
     * @param curriculum Curriculum ID
     * @param title Curriculum title
     * @param level Education level
     * @param week Study week
     * @param version Curriculum version
     * @param year Academic year
     * @param description Description
     * @return Redirect to curriculum
     * @throws Exception If update fails
     */
    @PostMapping("/{curriculum}/edit")
    public String edit(
        @PathVariable final long curriculum,
        @RequestParam final String title,
        @RequestParam(name = "level") final Curriculum.Level level,
        @RequestParam(name = "week") final Curriculum.Week week,
        @RequestParam final String version,
        @RequestParam(name = "year") final String year,
        @RequestParam(required = false) final String description
    ) throws Exception {
        this.curriculum(curriculum)
            .retitled(title.trim())
            .releveled(level)
            .reweeked(week)
            .reversioned(version.trim())
            .reyeared(year.trim()).redescriptioned(
                Optional.ofNullable(description).map(String::trim).orElse(null)
            );
        return String.format("redirect:/federal/curriculums/%d", curriculum);
    }
}
