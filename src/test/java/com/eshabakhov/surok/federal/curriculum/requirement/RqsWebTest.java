/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Filters;
import com.eshabakhov.surok.Page;
import com.eshabakhov.surok.Sort;
import com.eshabakhov.surok.Sorts;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.surok.filter.FilterRequest;
import com.eshabakhov.surok.filter.FiltersRequest;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.sort.SortRequest;
import com.eshabakhov.surok.sort.SortsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RqsWeb}.
 *
 * @since 0.0.1
 */
final class RqsWebTest {

    @Test
    void delegatesCreation() throws Exception {
        MatcherAssert.assertThat(
            "Web requirements must delegate creation",
            new RqsWeb(
                new RqsFake(),
                new FiltersRequest(Set.of()),
                new SortsRequest(List.of())
            ).create(new RqInputGrade(new RqInput(), "5")).uid(),
            Matchers.is(1L)
        );
    }

    @Test
    void delegatesLookup() throws Exception {
        MatcherAssert.assertThat(
            "Web requirements must delegate lookup",
            new RqsWeb(
                new RqsFake(
                    new LinkedHashMap<>(
                        Map.of(4L, new RqFake(4L, new RqInput()))
                    ),
                    new AtomicLong(4L)
                ),
                new FiltersRequest(Set.of()),
                new SortsRequest(List.of())
            ).requirement(4L).uid(),
            Matchers.is(4L)
        );
    }

    @Test
    void delegatesSelection() throws Exception {
        final Filters filters = new FiltersRequest(Set.<Filter>of());
        final Page page = new PageRequest(5, 1);
        final Sorts sorts = new SortsRequest(List.<Sort>of());
        MatcherAssert.assertThat(
            "Web requirements must delegate selection",
            new RqsWeb(
                new RqsFake(
                    new LinkedHashMap<>(
                        Map.of(3L, new RqFake(3L, new RqInput()))
                    ),
                    new AtomicLong(3L)
                ),
                filters,
                sorts
            ).selection(filters, page, sorts).iterate()
                .iterator().next().uid(),
            Matchers.is(3L)
        );
    }

    @Test
    void delegatesIteration() {
        final Requirement item = new RqFake(2L, new RqInput());
        MatcherAssert.assertThat(
            "Web requirements must delegate iteration",
            new RqsWeb(
                new RqsFake(
                    new LinkedHashMap<>(Map.of(2L, item)),
                    new AtomicLong(2L)
                ),
                new FiltersRequest(Set.of()),
                new SortsRequest(List.of())
            ).iterate(),
            Matchers.contains(Matchers.sameInstance(item))
        );
    }

    @Test
    void delegatesRemoval() throws Exception {
        final Map<Long, Requirement> memory = new LinkedHashMap<>(
            Map.of(8L, new RqFake(8L, new RqInput()))
        );
        new RqsWeb(
            new RqsFake(memory, new AtomicLong(8L)),
            new FiltersRequest(Set.of()),
            new SortsRequest(List.of())
        ).remove(8L);
        MatcherAssert.assertThat(
            "Web removal must update origin memory",
            memory,
            Matchers.anEmptyMap()
        );
    }

    @Test
    void printsFiltersAndSorts() throws Exception {
        MatcherAssert.assertThat(
            "Web requirements must append filter and sort state",
            new RqsWeb(
                new RqsFake(),
                new FiltersRequest(
                    Set.of(new FilterRequest("grade", "7"))
                ),
                new SortsRequest(
                    List.of(
                        new SortRequest("subject", Sort.Direction.DESC)
                    )
                )
            ).print(new MdJson()).print(),
            Matchers.is(
                new ObjectMapper().readTree(
                    """
                        {
                            "items": [],
                            "filters": [{"name": "grade", "value": "7"}],
                            "sorts": [{"name": "subject", "direction": "DESC"}]
                        }
                    """
                )
            )
        );
    }
}
