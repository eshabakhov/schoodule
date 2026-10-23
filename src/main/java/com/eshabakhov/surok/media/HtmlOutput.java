/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.media;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats JAXP HTML output without changing text or attribute values.
 *
 * <p>The serializer supplies the legacy-compatible doctype, an uppercase META
 * element and minimized boolean attributes. Formatting is shared by HTTP
 * responses and the standalone HTML checks.</p>
 *
 * @since 0.0.1
 */
public final class HtmlOutput implements UnaryOperator<String> {

    /** Tags, comments and declarations, with quoted attribute values kept intact. */
    private static final Pattern TAG = Pattern.compile(
        String.join(
            "",
            "(<!--(?s:.*?)-->)|(<![^>]*>)|<(/?)([A-Za-z][\\w:.-]*)",
            "((?:[^\"'<>]|\"[^\"]*\"|'[^']*')*)>"
        )
    );

    /** Serialized attributes, including minimized boolean attributes. */
    private static final Pattern ATTRIBUTE = Pattern.compile(
        "([\\w:.-]+)(?:\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s\"'>/]+))?"
    );

    /** Attribute order required by HTMLHint. */
    private static final List<String> ORDER = List.of(
        "class", "id", "name", "src", "for", "type", "rel", "href", "value", "title", "alt", "role"
    );

    /** Elements whose contents must not be interpreted as markup. */
    private static final List<String> RAW = List.of("script", "style", "textarea", "title");

    /** New HTML output formatter. */
    public HtmlOutput() {
        // Intentionally empty.
    }

    @Override
    public String apply(final String html) {
        final Matcher tags = HtmlOutput.TAG.matcher(html);
        final StringBuilder result = new StringBuilder(html.length());
        String raw = "";
        while (tags.find()) {
            String printed = tags.group();
            final String name = tags.group(4);
            if (raw.isEmpty() && "<!DOCTYPE html SYSTEM \"about:legacy-compat\">".equals(printed)) {
                printed = "<!DOCTYPE html>";
            } else if (raw.isEmpty() && name != null && tags.group(3).isEmpty()) {
                printed = HtmlOutput.tag(name, tags.group(5));
                if (HtmlOutput.RAW.contains(name)) {
                    raw = name;
                }
            } else if (raw.equals(name) && "/".equals(tags.group(3))) {
                raw = "";
            }
            tags.appendReplacement(result, Matcher.quoteReplacement(printed));
        }
        tags.appendTail(result);
        return result.toString();
    }

    private static String tag(final String name, final String source) {
        final List<Map.Entry<String, String>> attributes = new ArrayList<>(0);
        final Matcher values = HtmlOutput.ATTRIBUTE.matcher(source);
        while (values.find()) {
            String value = values.group(2);
            if (value == null) {
                value = "\"\"";
            }
            attributes.add(Map.entry(values.group(1), value));
        }
        attributes.sort(
            Comparator.comparingInt(
                (Map.Entry<String, String> attribute) -> HtmlOutput.rank(attribute.getKey())
            ).thenComparing(Map.Entry::getKey)
        );
        final StringBuilder printed = new StringBuilder(source.length() + name.length() + 2);
        String tag = name;
        if ("META".equals(name)) {
            tag = "meta";
        }
        printed.append('<').append(tag);
        for (final Map.Entry<String, String> attribute : attributes) {
            printed.append(' ').append(attribute.getKey()).append('=').append(attribute.getValue());
        }
        if (source.endsWith("/")) {
            printed.append('/');
        }
        return printed.append('>').toString();
    }

    private static int rank(final String name) {
        int rank = HtmlOutput.ORDER.indexOf(name);
        if (rank < 0) {
            rank = HtmlOutput.ORDER.size();
            if (name.startsWith("data-")) {
                rank += 1;
            }
        }
        return rank;
    }
}
