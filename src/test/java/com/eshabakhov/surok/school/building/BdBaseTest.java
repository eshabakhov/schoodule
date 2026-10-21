/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building;

import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.school.building.input.BdInput;
import com.eshabakhov.surok.school.building.input.BdInputName;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * In-memory building revisions.
 *
 * @since 0.0.1
 */
final class BdBaseTest {

    @Test
    void revisesWithoutChangingOriginal() throws Exception {
        final Building original = new BdBase(7L, "North");
        original.revise(new BdInputName(new BdInput(), "South"));
        MatcherAssert.assertThat(
            "An in-memory revision must leave the original value intact",
            original.print(new MdJson()).print().get("name").asText(),
            Matchers.is("North")
        );
    }

    @Test
    void preservesIdentityWhenRevising() throws Exception {
        MatcherAssert.assertThat(
            "Revision must retain the original building identity",
            new BdBase(7L, "North")
                .revise(new BdInputName(new BdInput(), "South")).uid(),
            Matchers.is(7L)
        );
    }
}
