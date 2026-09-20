/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.web.servlet.ModelAndView;

/**
 * A {@link Media} implementation that collects printed data
 * into a flat {@link Map} suitable for Thymeleaf model attributes.
 *
 * <p>Usage in a controller:
 * <pre>
 *   ThymeleafMedia media = new ThymeleafMedia("curriculums/details", "curriculum");
 *   curriculum.print(media);
 *   return media.view();
 * </pre>
 *
 * @since 0.0.1
 */
public final class ThymeleafMedia implements ViewMedia {

    /**
     * Template name.
     */
    private final String template;

    /**
     * Model attribute name.
     */
    private final String attribute;

    /**
     * Collected key-value pairs.
     */
    private final Map<String, Object> data;

    /**
     * Page model attributes.
     */
    private final Map<String, Object> model;

    /**
     * Page title format.
     */
    private String pattern;

    /**
     * Creates media for a Thymeleaf view.
     *
     * @param template Template name
     * @param attribute Model attribute name
     */
    public ThymeleafMedia(final String template, final String attribute) {
        this.template = template;
        this.attribute = attribute;
        this.data = new LinkedHashMap<>();
        this.model = new LinkedHashMap<>();
        this.pattern = "%s";
    }

    @Override
    public ThymeleafMedia attributes(
        final Map<String, Object> values
    ) {
        this.model.putAll(values);
        return this;
    }

    @Override
    public ThymeleafMedia title(final String format) {
        this.pattern = format;
        return this;
    }

    @Override
    public ThymeleafMedia with(final String name, final String value) {
        this.data.put(name, value);
        return this;
    }

    @Override
    public ThymeleafMedia with(final String name, final Long value) {
        this.data.put(name, value);
        return this;
    }

    @Override
    public ThymeleafMedia with(final String name, final Integer value) {
        this.data.put(name, value);
        return this;
    }

    @Override
    public ThymeleafMedia include(final String... names) {
        this.data.keySet().retainAll(Set.of(names));
        return this;
    }

    /**
     * Returns the collected data as an unmodifiable map
     * ready to be added to a Thymeleaf {@code ModelAndView}.
     *
     * @return Unmodifiable map of field name to value
     */
    public Map<String, Object> map() {
        return Collections.unmodifiableMap(this.data);
    }

    @Override
    public ModelAndView view() {
        final ModelAndView view = new ModelAndView(this.template)
            .addAllObjects(this.model);
        if (this.attribute.isBlank()) {
            view.addAllObjects(this.data);
        } else {
            view.addObject(this.attribute, this.map());
        }
        if (
            this.data.containsKey("title")
                && !this.model.containsKey("pageTitle")
        ) {
            view.addObject(
                "pageTitle",
                String.format(this.pattern, this.data.get("title"))
            );
        }
        return view;
    }
}
