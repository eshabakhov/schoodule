/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Sort;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.surok.filter.FiltersRequest;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.page.ResponsePage;
import com.eshabakhov.surok.sort.SortsRequest;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RqsSelected}.
 *
 * @since 0.0.1
 */
final class RqsSelectedTest {

    @Test
    void delegatesCreation() throws Exception {
        MatcherAssert.assertThat(
            "Selected requirements must delegate creation",
            new RqsSelected(
                new RqsFake(),
                List.of(),
                new ResponsePage(new PageRequest(15, 1), 0)
            ).create(new RqInputGrade(new RqInput(), "5")).uid(),
            Matchers.is(1L)
        );
    }

    @Test
    void delegatesLookup() throws Exception {
        MatcherAssert.assertThat(
            "Selected requirements must delegate lookup",
            new RqsSelected(
                new RqsFake(
                    new LinkedHashMap<>(
                        Map.of(5L, new RqFake(5L, new RqInput()))
                    ),
                    new AtomicLong(5L)
                ),
                List.of(),
                new ResponsePage(new PageRequest(15, 1), 0)
            ).requirement(5L).uid(),
            Matchers.is(5L)
        );
    }

    @Test
    void delegatesSelection() throws Exception {
        MatcherAssert.assertThat(
            "Selected requirements must delegate another selection",
            new RqsSelected(
                new RqsFake(
                    new LinkedHashMap<>(
                        Map.of(6L, new RqFake(6L, new RqInput()))
                    ),
                    new AtomicLong(6L)
                ),
                List.of(),
                new ResponsePage(new PageRequest(15, 1), 0)
            ).selection(
                new FiltersRequest(Set.<Filter>of()),
                new PageRequest(10, 1),
                new SortsRequest(List.<Sort>of())
            ).iterate().iterator().next().uid(),
            Matchers.is(6L)
        );
    }

    @Test
    void exposesSelectedItems() {
        final Iterable<Requirement> items = List.of(
            new RqFake(1L, new RqInput())
        );
        MatcherAssert.assertThat(
            "Selected requirements must expose their items",
            new RqsSelected(
                new RqsFake(),
                items,
                new ResponsePage(new PageRequest(15, 1), 1)
            ).iterate(),
            Matchers.sameInstance(items)
        );
    }

    @Test
    void printsItemsAndPage() throws Exception {
        MatcherAssert.assertThat(
            "Selected requirements must print items and pagination",
            new RqsSelected(
                new RqsFake(),
                List.of(new RqV1(new RqFake(3L, new RqInput()))),
                new ResponsePage(new PageRequest(10, 2), 25)
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode().<ObjectNode>set(
                    "items",
                    JsonNodeFactory.instance.arrayNode().add(
                        JsonNodeFactory.instance.objectNode().put("id", 3L)
                    )
                ).<ObjectNode>set(
                    "page",
                    JsonNodeFactory.instance.objectNode()
                        .put("limit", 10)
                        .put("offset", 2)
                        .put("total", 25)
                        .put("totalPages", 3)
                        .put("hasNext", true)
                        .put("hasPrev", true)
                )
            )
        );
    }

    @Test
    void delegatesRemoval() throws Exception {
        final Map<Long, Requirement> memory = new LinkedHashMap<>(
            Map.of(9L, new RqFake(9L, new RqInput()))
        );
        new RqsSelected(
            new RqsFake(memory, new AtomicLong(9L)),
            List.of(),
            new ResponsePage(new PageRequest(15, 1), 0)
        ).remove(9L);
        MatcherAssert.assertThat(
            "Selected removal must update origin memory",
            memory,
            Matchers.anEmptyMap()
        );
    }
}
