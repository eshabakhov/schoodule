/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Printable;
import com.eshabakhov.schoodule.ResultPage;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;

/**
 * A {@link Media} implementation that collects printed data
 * into a Jackson {@link ObjectNode} suitable for REST responses.
 *
 * <p>Usage in a REST controller:</p>
 * <pre>
 *   JsonMedia media = new JsonMedia();
 *   curriculum.print(media);
 *   return ResponseEntity.ok(media.json());
 * </pre>
 *
 * @since 0.0.1
 */
public final class JsonMedia implements com.eshabakhov.schoodule.JsonMedia {

    /**
     * Jackson object node being filled.
     */
    private final ObjectNode node;

    /**
     * Creates an empty JsonMedia.
     */
    public JsonMedia() {
        this.node = new ObjectNode(JsonNodeFactory.instance);
    }

    @Override
    public JsonMedia with(final String name, final String value) {
        this.node.put(name, value);
        return this;
    }

    @Override
    public JsonMedia with(final String name, final Long value) {
        this.node.put(name, value);
        return this;
    }

    @Override
    public JsonMedia with(final String name, final Integer value) {
        this.node.put(name, value);
        return this;
    }

    @Override
    public JsonMedia with(final String name, final Boolean value) {
        this.node.put(name, value);
        return this;
    }

    @Override
    public JsonMedia with(
        final String name,
        final Iterable<? extends Printable> values
    ) {
        final ArrayNode array = JsonNodeFactory.instance.arrayNode();
        values.forEach(value -> array.add(value.print(new JsonMedia()).json()));
        this.node.set(name, array);
        return this;
    }

    @Override
    public JsonMedia with(final String name, final ResultPage value) {
        this.node.set(
            name,
            JsonNodeFactory.instance.objectNode()
                .put("limit", value.limit())
                .put("offset", value.offset())
                .put("total", value.total())
                .put("totalPages", value.totalPages())
                .put("hasNext", value.hasNext())
                .put("hasPrev", value.hasPrev())
        );
        return this;
    }

    @Override
    public JsonMedia include(final String... names) {
        final Set<String> allowed = Set.of(names);
        this.node.fieldNames().forEachRemaining(
            field -> {
                if (!allowed.contains(field)) {
                    this.node.remove(field);
                }
            }
        );
        return this;
    }

    @Override
    public ObjectNode json() {
        return this.node;
    }
}
