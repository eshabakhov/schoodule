/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link Media} implementation that collects printed data
 * into a flat {@link Map} suitable for Thymeleaf model attributes.
 *
 * <p>Usage in a controller:
 * <pre>
 *   ThymeleafMedia media = new ThymeleafMedia();
 *   curriculum.print(media);
 *   modelAndView.addAllObjects(media.map());
 * </pre>
 *
 * @since 0.0.1
 */
public final class ThymeleafMedia implements Media {

    /**
     * Collected key-value pairs.
     */
    private final Map<String, Object> data;

    /**
     * Creates an empty ThymeleafMedia.
     */
    public ThymeleafMedia() {
        this(new LinkedHashMap<>());
    }

    /**
     * Creates a ThymeleafMedia with pre-filled data (for immutable chaining).
     *
     * @param data Existing data map
     */
    private ThymeleafMedia(final Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public Media with(final String name, final String value) {
        final Map<String, Object> copy = new LinkedHashMap<>(this.data);
        copy.put(name, value);
        return new ThymeleafMedia(copy);
    }

    @Override
    public Media with(final String name, final Long value) {
        final Map<String, Object> copy = new LinkedHashMap<>(this.data);
        copy.put(name, value);
        return new ThymeleafMedia(copy);
    }

    @Override
    public Media with(final String name, final Integer value) {
        final Map<String, Object> copy = new LinkedHashMap<>(this.data);
        copy.put(name, value);
        return new ThymeleafMedia(copy);
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
}
