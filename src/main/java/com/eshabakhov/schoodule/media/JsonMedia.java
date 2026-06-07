/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@link Media} implementation that collects printed data
 * into a Jackson {@link ObjectNode} suitable for REST responses.
 *
 * <p>Usage in a REST controller:
 * <pre>
 *   JsonMedia media = new JsonMedia();
 *   curriculum.print(media);
 *   return ResponseEntity.ok(media.json());
 * </pre>
 *
 * @since 0.0.1
 */
public final class JsonMedia implements Media {

    /**
     * Jackson object node being filled.
     */
    private final ObjectNode node;

    /**
     * Creates an empty JsonMedia.
     */
    public JsonMedia() {
        this.node = JsonNodeFactory.instance.objectNode();
    }

    @Override
    public Media with(final String name, final String value) {
        this.node.put(name, value);
        return this;
    }

    @Override
    public Media with(final String name, final Long value) {
        this.node.put(name, value);
        return this;
    }

    @Override
    public Media with(final String name, final Integer value) {
        this.node.put(name, value);
        return this;
    }

    /**
     * Returns the filled Jackson {@link ObjectNode}.
     *
     * @return ObjectNode with all printed fields
     */
    public ObjectNode json() {
        return this.node;
    }
}
