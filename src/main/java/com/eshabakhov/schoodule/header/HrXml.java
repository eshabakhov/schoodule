/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.header;

import com.eshabakhov.schoodule.Header;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.media.MdXml;
import javax.xml.transform.Source;

/**
 * XML representation header.
 *
 * @since 0.0.1
 */
public final class HrXml implements Header<Source> {

    /** Requested media subtype. */
    private final String subtype;

    /**
     * New XML header.
     *
     * @param subtype Requested media subtype
     */
    public HrXml(final String subtype) {
        this.subtype = subtype;
    }

    @Override
    public Media<Source> media(
        final String template,
        final String attribute
    ) {
        final Media<Source> media = new MdXml();
        final Media<Source> result;
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
