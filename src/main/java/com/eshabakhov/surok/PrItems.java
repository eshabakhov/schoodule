/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok;

/**
 * Printable collection items.
 *
 * @since 0.0.1
 */
public final class PrItems implements Printable {

    /** Item name. */
    private final String name;

    /** Items to print. */
    private final Iterable<? extends Printable> items;

    /**
     * New printable items.
     *
     * @param name Item name
     * @param items Items to print
     */
    public PrItems(
        final String name,
        final Iterable<? extends Printable> items
    ) {
        this.name = name;
        this.items = items;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        Media<T> printed = media;
        for (final Printable item : this.items) {
            printed = printed.with(this.name, item);
        }
        return printed;
    }
}
