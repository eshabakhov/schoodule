/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.page;

import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PageableList;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * Simple implementation of {@link PageableList}.
 *
 * @param <T> Type of elements stored in the list.
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class ResponsePageableList<T> implements PageableList<T> {

    /** List of objects. */
    private final List<T> lst;

    /** Total elements. */
    private final Integer ttl;

    /** Page implementation. */
    private final Page page;

    public ResponsePageableList(final List<T> list, final Integer total, final Page page) {
        this.lst = list;
        this.ttl = total;
        this.page = page;
    }

    @Override
    public List<T> list() {
        return this.lst;
    }

    @Override
    public Integer total() {
        return this.ttl;
    }

    @Override
    public Integer limit() {
        return this.page.limit();
    }

    @Override
    public Integer offset() {
        return this.page.offset();
    }
}
