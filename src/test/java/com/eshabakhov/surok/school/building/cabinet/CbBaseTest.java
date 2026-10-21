/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.cabinet;

import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.school.building.cabinet.input.CbInput;
import com.eshabakhov.surok.school.building.cabinet.input.CbInputName;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * In-memory cabinet revisions.
 *
 * @since 0.0.1
 */
final class CbBaseTest {

    @Test
    void revisesWithoutChangingOriginal() throws Exception {
        final Cabinet original = new CbBase(7L, "North");
        original.revise(new CbInputName(new CbInput(), "South"));
        MatcherAssert.assertThat(
            "An in-memory revision must leave the original value intact",
            original.print(new MdJson()).print().get("name").asText(),
            Matchers.is("North")
        );
    }

    @Test
    void preservesIdentityWhenRevising() throws Exception {
        MatcherAssert.assertThat(
            "Revision must retain the original cabinet identity",
            new CbBase(7L, "North")
                .revise(new CbInputName(new CbInput(), "South")).uid(),
            Matchers.is(7L)
        );
    }
}
