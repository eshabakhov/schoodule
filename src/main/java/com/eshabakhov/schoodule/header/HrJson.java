/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.header;

import com.eshabakhov.schoodule.Header;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON representation header.
 *
 * @since 0.0.1
 */
public final class HrJson implements Header<ObjectNode> {

    /** Requested media subtype. */
    private final String subtype;

    /**
     * New JSON header.
     *
     * @param subtype Requested media subtype
     */
    public HrJson(final String subtype) {
        this.subtype = subtype;
    }

    @Override
    public Media<ObjectNode> media(
        final String template,
        final String attribute
    ) {
        final Media<ObjectNode> media = new MdJson();
        final Media<ObjectNode> result;
        if (this.subtype.contains("federal-curriculum.simple.")) {
            result = media.include(
                "id", "title", "level", "week", "version", "year"
            );
        } else if (this.subtype.contains("federal-curriculum.full.")) {
            result = media.include(
                "id", "title", "level", "week", "version", "year",
                "description"
            );
        } else if (
            this.subtype.contains("federal-curriculum-requirement.simple.")
        ) {
            result = media.include(
                "id", "grade", "subjectName", "weeklyHours", "partType"
            );
        } else {
            result = media;
        }
        return result;
    }
}
