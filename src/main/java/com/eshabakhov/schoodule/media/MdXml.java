/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.ResultPage;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.web.util.HtmlUtils;

/**
 * XML implementation of {@link Media}.
 *
 * @since 0.0.1
 */
public final class MdXml implements Media<String> {

    /** XML root element. */
    private final String root;

    /** Collected XML elements. */
    private final Map<String, String> elements;

    /** Included fields. */
    private final Set<String> fields;

    /** Whether the included fields are restricted. */
    private final boolean restricted;

    /** XML element format. */
    private final String pattern;

    /**
     * New XML media with the specified root element.
     *
     * @param root Root element
     */
    public MdXml(final String root) {
        this(
            root,
            new LinkedHashMap<>(0),
            new LinkedHashSet<>(0),
            false,
            "<%1$s>%2$s</%1$s>"
        );
    }

    /**
     * New XML media with collected values.
     *
     * @param root Root element
     * @param elements Collected XML elements
     * @param fields Included fields
     * @param restricted Whether fields are restricted
     * @param pattern XML element format
     */
    private MdXml(
        final String root,
        final Map<String, String> elements,
        final Set<String> fields,
        final boolean restricted,
        final String pattern
    ) {
        this.root = root;
        this.elements = elements;
        this.fields = fields;
        this.restricted = restricted;
        this.pattern = pattern;
    }

    @Override
    public MdXml with(final String name, final String value) {
        String printed = "";
        if (value != null) {
            printed = HtmlUtils.htmlEscape(value);
        }
        final Map<String, String> copied = new LinkedHashMap<>(this.elements);
        if (this.allowed(name)) {
            copied.put(name, this.element(name, printed));
        }
        return new MdXml(
            this.root,
            Collections.unmodifiableMap(copied),
            this.fields,
            this.restricted,
            this.pattern
        );
    }

    @Override
    public MdXml with(final String name, final Long value) {
        String printed = "";
        if (value != null) {
            printed = value.toString();
        }
        return this.with(name, printed);
    }

    @Override
    public MdXml with(final String name, final Integer value) {
        String printed = "";
        if (value != null) {
            printed = value.toString();
        }
        return this.with(name, printed);
    }

    @Override
    public MdXml with(final String name, final Boolean value) {
        String printed = "";
        if (value != null) {
            printed = value.toString();
        }
        return this.with(name, printed);
    }

    @Override
    public MdXml with(final String name, final Printable value)
        throws Exception {
        final Map<String, String> copied = new LinkedHashMap<>(this.elements);
        if (this.allowed(name)) {
            copied.merge(
                name,
                value.print(new MdXml(name)).print(),
                String::concat
            );
        }
        return new MdXml(
            this.root,
            Collections.unmodifiableMap(copied),
            this.fields,
            this.restricted,
            this.pattern
        );
    }

    @Override
    public MdXml with(final Printable value) throws Exception {
        return (MdXml) value.print(this);
    }

    @Override
    public MdXml with(final String name, final ResultPage value) {
        final Map<String, String> copied = new LinkedHashMap<>(this.elements);
        if (this.allowed(name)) {
            copied.put(
                name,
                this.element(
                    name,
                    this.element("limit", Integer.toString(value.limit()))
                        + this.element(
                            "offset", Integer.toString(value.offset())
                        )
                        + this.element("total", Integer.toString(value.total()))
                        + this.element(
                            "totalPages",
                            Integer.toString(value.totalPages())
                        )
                        + this.element(
                            "hasNext",
                            Boolean.toString(value.hasNext())
                        )
                        + this.element(
                            "hasPrev",
                            Boolean.toString(value.hasPrev())
                        )
                )
            );
        }
        return new MdXml(
            this.root,
            Collections.unmodifiableMap(copied),
            this.fields,
            this.restricted,
            this.pattern
        );
    }

    @Override
    public MdXml attributes(final Map<String, Object> values) {
        return this;
    }

    @Override
    public MdXml title(final String format) {
        return this;
    }

    @Override
    public MdXml include(final String... names) {
        final Set<String> included = new LinkedHashSet<>(
            Arrays.asList(names)
        );
        final Map<String, String> copied = new LinkedHashMap<>(this.elements);
        copied.keySet().retainAll(included);
        return new MdXml(
            this.root,
            Collections.unmodifiableMap(copied),
            Collections.unmodifiableSet(included),
            true,
            this.pattern
        );
    }

    @Override
    public String print() {
        return this.xml();
    }

    private boolean allowed(final String name) {
        return !this.restricted || this.fields.contains(name);
    }

    private String element(final String name, final String value) {
        return String.format(this.pattern, name, value);
    }

    private String xml() {
        return this.element(this.root, String.join("", this.elements.values()));
    }
}
