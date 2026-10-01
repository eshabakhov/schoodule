/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.ResultPage;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirement;
import com.eshabakhov.schoodule.federal.curriculum.FederalCurriculumRequirements;
import lombok.EqualsAndHashCode;

/**
 * Selected federal curriculum requirements.
 *
 * @since 0.0.1
 */
@EqualsAndHashCode
public final class FcrsSelected implements FederalCurriculumRequirements {

    /** Complete collection. */
    private final FederalCurriculumRequirements origin;

    /** Selected requirements. */
    private final Iterable<FederalCurriculumRequirement> items;

    /** Selection pagination. */
    private final ResultPage page;

    public FcrsSelected(
        final FederalCurriculumRequirements origin,
        final Iterable<FederalCurriculumRequirement> items,
        final ResultPage page
    ) {
        this.origin = origin;
        this.items = items;
        this.page = page;
    }

    @Override
    public FederalCurriculumRequirement create(
        final Integer grade,
        final String subject,
        final Integer hours,
        final FederalCurriculumRequirement.PartType part
    ) throws Exception {
        return this.origin.create(grade, subject, hours, part);
    }

    @Override
    public FederalCurriculumRequirement requirement(final long id)
        throws Exception {
        return this.origin.requirement(id);
    }

    @Override
    public FederalCurriculumRequirements selection(
        final Filters filters,
        final Page requested,
        final Sorts sorts
    ) throws Exception {
        return this.origin.selection(filters, requested, sorts);
    }

    @Override
    public Iterable<FederalCurriculumRequirement> iterate() {
        return this.items;
    }

    @Override
    public <M extends Media> M print(final M media) {
        media.with("items", this.items).with("page", this.page);
        return media;
    }

    @Override
    public void remove(final long id) throws Exception {
        this.origin.remove(id);
    }
}
