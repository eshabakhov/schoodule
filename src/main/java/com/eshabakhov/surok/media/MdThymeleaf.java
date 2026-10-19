/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.ResultPage;
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
    public MdThymeleaf with(final String name, final Printable value)
        throws Exception {
        final Map<String, Object> printed = Collections.unmodifiableMap(
            new LinkedHashMap<>(
                value.print(new MdThymeleaf("", ""))
                    .print().getModel()
            )
        );
        final Object rendered;
        if ("item".equals(name) || this.data.containsKey(name)) {
            final Object current = this.data.getOrDefault(
                name, Collections.emptyList()
            );
            final List<Object> items = new ArrayList<>(0);
            if (List.class.isAssignableFrom(current.getClass())) {
                items.addAll((List<?>) current);
            } else {
                items.add(current);
            }
            items.add(printed);
            rendered = Collections.unmodifiableList(items);
        } else if (printed.size() == 1 && printed.containsKey("item")) {
            rendered = printed.get("item");
        } else {
            rendered = printed;
        }
        return this.with(name, rendered);
    }

    @Override
    public MdThymeleaf with(final Printable value) throws Exception {
        final Map<String, Object> printed = value.print(
            new MdThymeleaf("", "")
        ).print().getModel();
        final List<Object> items = new ArrayList<>(0);
        if (!printed.isEmpty()) {
            final Object selected = printed.values().iterator().next();
            if (List.class.isAssignableFrom(selected.getClass())) {
                items.addAll((List<?>) selected);
            } else {
                items.add(selected);
            }
        }
        return this.with("items", Collections.unmodifiableList(items));
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
