/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school;

import com.eshabakhov.surok.Media;
import com.eshabakhov.surok.PrItems;
import com.eshabakhov.surok.Printable;
import com.eshabakhov.surok.School;
import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.school.schedule.SdBrief;
import org.jooq.impl.DSL;

/**
 * School navigation data, for example {@code new SlBrief(school).print(media)}.
 *
 * @since 0.0.1
 */
public final class SlBrief implements Printable {

    /** Selected school. */
    private final School origin;

    /**
     * New school representation.
     *
     * @param origin Selected school
     */
    public SlBrief(final School origin) {
        this.origin = origin;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with("id", this.origin.uid()).with("name", this.origin.name()).with(
            "schedules",
            new PrItems(
                "schedule",
                this.origin.schedules().schedules(
                    DSL.trueCondition(), new PageRequest(Integer.MAX_VALUE, 1)
                ).list().stream().map(SdBrief::new).toList()
            )
        );
    }
}
