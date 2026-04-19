/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Pageable list.
 *
 * @param <T> Type of elements stored in the list.
 * @since 0.0.1
 */
public interface PageableList<T> {

    /**
     * List of {@code T}.
     *
     * @return List of {@code T}
     */
    @JsonProperty("list")
    List<T> list();

    /**
     * Total elements in the whole list.
     *
     * @return Total elements
     */
    @JsonProperty("total")
    Integer total();

    /**
     * Limit for querying elements in the whole list.
     *
     * @return Limit of list
     */
    @JsonProperty("limit")
    Integer limit();

    /**
     * Offset for querying elements in the whole list.
     *
     * @return Offset of list
     */
    @JsonProperty("offset")
    Integer offset();
}
