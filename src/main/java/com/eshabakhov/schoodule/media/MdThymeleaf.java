/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.ResultPage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.servlet.ModelAndView;

/**
 * Thymeleaf implementation of {@link Media}.
 *
 * @since 0.0.1
 */
public final class MdThymeleaf implements Media<ModelAndView> {

    /** Template name. */
    private final String template;

    /** Model attribute name. */
    private final String attribute;

    /** Collected key-value pairs. */
    private final Map<String, Object> data;

    /** Page model attributes. */
    private final Map<String, Object> model;

    /** Page title format. */
    private final String pattern;

    /**
     * Creates media for a Thymeleaf view.
     *
     * @param template Template name
     * @param attribute Model attribute name
     */
    public MdThymeleaf(final String template, final String attribute) {
        this(
            template,
            attribute,
            new LinkedHashMap<>(0),
            new LinkedHashMap<>(0),
            "%s"
        );
    }

    /**
     * Creates Thymeleaf media with collected values.
     *
     * @param template Template name
     * @param attribute Model attribute name
     * @param data Collected values
     * @param model Page attributes
     * @param pattern Page title format
     */
    private MdThymeleaf(
        final String template,
        final String attribute,
        final Map<String, Object> data,
        final Map<String, Object> model,
        final String pattern
    ) {
        this.template = template;
        this.attribute = attribute;
        this.data = data;
        this.model = model;
        this.pattern = pattern;
    }

    @Override
    public MdThymeleaf attributes(final Map<String, Object> values) {
        final Map<String, Object> copied = new LinkedHashMap<>(this.model);
        copied.putAll(values);
        return new MdThymeleaf(
            this.template,
            this.attribute,
            this.data,
            Collections.unmodifiableMap(copied),
            this.pattern
        );
    }

    @Override
    public MdThymeleaf title(final String format) {
        return new MdThymeleaf(
            this.template, this.attribute, this.data, this.model, format
        );
    }

    @Override
    public MdThymeleaf with(final String name, final String value) {
        return this.with(name, (Object) value);
    }

    @Override
    public MdThymeleaf with(final String name, final Long value) {
        return this.with(name, (Object) value);
    }

    @Override
    public MdThymeleaf with(final String name, final Integer value) {
        return this.with(name, (Object) value);
    }

    @Override
    public MdThymeleaf with(final String name, final Boolean value) {
        return this.with(name, (Object) value);
    }

    @Override
    public MdThymeleaf with(
        final String name,
        final Iterable<? extends Printable> values
    ) throws Exception {
        final List<Map<String, Object>> printed = new ArrayList<>(0);
        for (final Printable value : values) {
            printed.add(
                Collections.unmodifiableMap(
                    new LinkedHashMap<>(
                        value.print(new MdThymeleaf("", ""))
                            .print().getModel()
                    )
                )
            );
        }
        return this.with(name, printed);
    }

    @Override
    public MdThymeleaf with(final String name, final ResultPage value) {
        return this.with(name, (Object) value);
    }

    @Override
    public MdThymeleaf include(final String... names) {
        final Map<String, Object> copied = new LinkedHashMap<>(this.data);
        copied.keySet().retainAll(Set.of(names));
        return new MdThymeleaf(
            this.template,
            this.attribute,
            Collections.unmodifiableMap(copied),
            this.model,
            this.pattern
        );
    }

    @Override
    public ModelAndView print() {
        final ModelAndView view = new ModelAndView(this.template)
            .addAllObjects(this.model);
        if (this.attribute.isBlank()) {
            view.addAllObjects(this.data);
        } else {
            view.addObject(
                this.attribute,
                Collections.unmodifiableMap(new LinkedHashMap<>(this.data))
            );
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

    private MdThymeleaf with(final String name, final Object value) {
        final Map<String, Object> copied = new LinkedHashMap<>(this.data);
        copied.put(name, value);
        return new MdThymeleaf(
            this.template,
            this.attribute,
            Collections.unmodifiableMap(copied),
            this.model,
            this.pattern
        );
    }
}
