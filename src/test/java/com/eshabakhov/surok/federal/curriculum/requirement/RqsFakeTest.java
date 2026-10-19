/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.federal.curriculum.requirement;

import com.eshabakhov.surok.Filter;
import com.eshabakhov.surok.Sort;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInput;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputGrade;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputHours;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputPart;
import com.eshabakhov.surok.federal.curriculum.requirement.input.RqInputSubject;
import com.eshabakhov.surok.filter.FilterRequest;
import com.eshabakhov.surok.filter.FiltersRequest;
import com.eshabakhov.surok.media.MdJson;
import com.eshabakhov.surok.page.PageRequest;
import com.eshabakhov.surok.sort.SortRequest;
import com.eshabakhov.surok.sort.SortsRequest;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RqsFake}.
 *
 * @since 0.0.1
 */
final class RqsFakeTest {

    @Test
    void createsRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Created fake requirement must receive an identity",
            new RqsFake().create(
                new RqInputGrade(new RqInput(), "5")
            ).uid(),
            Matchers.is(1L)
        );
    }

    @Test
    void createsRequirementNextToDifferentOne() throws Exception {
        MatcherAssert.assertThat(
            "A different fake requirement must receive the next identity",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(
                        1L,
                        new RqFake(
                            1L, new RqInputGrade(new RqInput(), "5")
                        )
                    )
                ),
                new AtomicLong(1L)
            ).create(
                new RqInputGrade(new RqInput(), "6")
            ).uid(),
            Matchers.is(2L)
        );
    }

    @Test
    void findsRequirement() throws Exception {
        final Requirement requirement = new RqFake(1L, new RqInput());
        MatcherAssert.assertThat(
            "Fake requirement must be available by identity",
            new RqsFake(
                new LinkedHashMap<>(Map.of(1L, requirement)),
                new AtomicLong(1L)
            ).requirement(1L),
            Matchers.sameInstance(requirement)
        );
    }

    @Test
    void rejectsDuplicateRequirement() throws Exception {
        Assertions.assertThrows(
            RequirementAlreadyExistsException.class,
            () -> new RqsFake(
                new LinkedHashMap<>(
                    Map.of(
                        1L,
                        new RqFake(
                            1L, new RqInputGrade(new RqInput(), "5")
                        )
                    )
                ),
                new AtomicLong(1L)
            ).create(new RqInputGrade(new RqInput(), "5"))
        );
    }

    @Test
    void rejectsMissingRequirement() {
        Assertions.assertThrows(
            RequirementNotFoundException.class,
            () -> new RqsFake().requirement(1L)
        );
    }

    @Test
    void selectsFilteredAndSortedPage() throws Exception {
        MatcherAssert.assertThat(
            "Fake selection must apply filters, sorting and pagination",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(
                        1L,
                        new RqFake(
                            1L,
                            new RqInputPart(
                                new RqInputHours(
                                    new RqInputSubject(
                                        new RqInputGrade(new RqInput(), "5"),
                                        "History"
                                    ),
                                    "2"
                                ),
                                "OPTIONAL"
                            )
                        ),
                        2L,
                        new RqFake(
                            2L,
                            new RqInputPart(
                                new RqInputHours(
                                    new RqInputSubject(
                                        new RqInputGrade(new RqInput(), "7"),
                                        "Mathematics"
                                    ),
                                    "6"
                                ),
                                "MANDATORY"
                            )
                        ),
                        3L,
                        new RqFake(
                            3L,
                            new RqInputPart(
                                new RqInputHours(
                                    new RqInputSubject(
                                        new RqInputGrade(new RqInput(), "7"),
                                        "Mathematics"
                                    ),
                                    "6"
                                ),
                                "OPTIONAL"
                            )
                        )
                    )
                ),
                new AtomicLong(3L)
            ).selection(
                new FiltersRequest(
                    new LinkedHashSet<>(
                        List.of(
                            new FilterRequest("grade", "7"),
                            new FilterRequest("subjectName", " math "),
                            new FilterRequest("ignored", "value")
                        )
                    )
                ),
                new PageRequest(1, 1),
                new SortsRequest(
                    List.of(
                        new SortRequest("grade", Sort.Direction.ASC),
                        new SortRequest("subject", Sort.Direction.DESC),
                        new SortRequest("hours", Sort.Direction.ASC),
                        new SortRequest("part", Sort.Direction.DESC)
                    )
                )
            ).iterate().iterator().next().uid(),
            Matchers.is(3L)
        );
    }

    @Test
    void ignoresEmptyAndInactiveSelectionParameters() throws Exception {
        MatcherAssert.assertThat(
            "Empty filters and inactive sorts must preserve requirements",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(
                        4L,
                        new RqFake(
                            4L,
                            new RqInputPart(
                                new RqInputHours(
                                    new RqInputSubject(
                                        new RqInputGrade(new RqInput(), "8"),
                                        "Physics"
                                    ),
                                    "3"
                                ),
                                "OPTIONAL"
                            )
                        ),
                        5L,
                        new RqFake(
                            5L,
                            new RqInputPart(
                                new RqInputHours(
                                    new RqInputSubject(
                                        new RqInputGrade(new RqInput(), "8"),
                                        "Physics"
                                    ),
                                    "3"
                                ),
                                "MANDATORY"
                            )
                        )
                    )
                ),
                new AtomicLong(5L)
            ).selection(
                new FiltersRequest(
                    Set.of(
                        new FilterRequest("grade", null),
                        new FilterRequest("subject", " ")
                    )
                ),
                new PageRequest(10, 1),
                new SortsRequest(
                    List.of(
                        new SortRequest("subject", Sort.Direction.NONE),
                        new SortRequest("grade", null),
                        new SortRequest("unknown", Sort.Direction.ASC)
                    )
                )
            ).iterate().iterator().next().uid(),
            Matchers.is(5L)
        );
    }

    @Test
    void returnsEmptyPageAfterAvailableItems() throws Exception {
        MatcherAssert.assertThat(
            "Page after all fake requirements must be empty",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(1L, new RqFake(1L, new RqInput()))
                ),
                new AtomicLong(1L)
            ).selection(
                new FiltersRequest(Set.<Filter>of()),
                new PageRequest(1, 3),
                new SortsRequest(List.<Sort>of())
            ).iterate(),
            Matchers.emptyIterable()
        );
    }

    @Test
    void filtersOutRequirement() throws Exception {
        MatcherAssert.assertThat(
            "Non-matching fake requirement must be filtered out",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(
                        1L,
                        new RqFake(
                            1L,
                            new RqInputPart(
                                new RqInputSubject(
                                    new RqInputGrade(new RqInput(), "5"),
                                    "History"
                                ),
                                "OPTIONAL"
                            )
                        )
                    )
                ),
                new AtomicLong(1L)
            ).selection(
                new FiltersRequest(
                    Set.of(new FilterRequest("part", "MANDATORY"))
                ),
                new PageRequest(10, 1),
                new SortsRequest(List.<Sort>of())
            ).iterate(),
            Matchers.emptyIterable()
        );
    }

    @Test
    void printsRequirements() throws Exception {
        MatcherAssert.assertThat(
            "Fake requirements must print their items",
            new RqsFake(
                new LinkedHashMap<>(
                    Map.of(6L, new RqV1(new RqFake(6L, new RqInput())))
                ),
                new AtomicLong(6L)
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode().<ObjectNode>set(
                    "items",
                    JsonNodeFactory.instance.arrayNode().add(
                        JsonNodeFactory.instance.objectNode().put("id", 6L)
                    )
                )
            )
        );
    }

    @Test
    void removesRequirement() throws Exception {
        final Map<Long, Requirement> memory = new LinkedHashMap<>(
            Map.of(7L, new RqFake(7L, new RqInput()))
        );
        new RqsFake(memory, new AtomicLong(7L)).remove(7L);
        MatcherAssert.assertThat(
            "Removed requirement must leave fake memory",
            memory,
            Matchers.anEmptyMap()
        );
    }

    @Test
    void rejectsRemovalOfMissingRequirement() {
        Assertions.assertThrows(
            RequirementNotFoundException.class,
            () -> new RqsFake().remove(5L)
        );
    }
}
