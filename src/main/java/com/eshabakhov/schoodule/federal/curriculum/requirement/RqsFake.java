/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement;

import com.eshabakhov.schoodule.Filter;
import com.eshabakhov.schoodule.Filters;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.Page;
import com.eshabakhov.schoodule.PrItems;
import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import com.eshabakhov.schoodule.media.MdJson;
import com.eshabakhov.schoodule.page.ResponsePage;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory federal curriculum requirements.
 *
 * @since 0.0.1
 * @checkstyle CyclomaticComplexityCheck (1000 lines)
 * @checkstyle MissingNullCaseInSwitchCheck (1000 lines)
 * @checkstyle NPathComplexityCheck (1000 lines)
 */
@SuppressWarnings({
    "PMD.CognitiveComplexity",
    "PMD.NPathComplexity"
})
public final class RqsFake implements Requirements {

    /** Stored requirements. */
    private final Map<Long, Requirement> items;

    /** Requirement identity sequence. */
    private final AtomicLong identities;

    /**
     * New empty in-memory requirements.
     */
    public RqsFake() {
        this(new LinkedHashMap<>(0), new AtomicLong(0L));
    }

    /**
     * New in-memory requirements.
     *
     * @param items Stored requirements
     * @param identities Requirement identity sequence
     */
    public RqsFake(
        final Map<Long, Requirement> items,
        final AtomicLong identities
    ) {
        this.items = items;
        this.identities = identities;
    }

    @Override
    public Requirement create(final RequirementInput input) throws Exception {
        final ObjectNode requested = input.print(new MdJson()).print();
        for (final Requirement item : this.items.values()) {
            final ObjectNode existing = item.print(new MdJson()).print();
            existing.remove(List.of("id", "internal"));
            if (existing.equals(requested)) {
                throw new RequirementAlreadyExistsException();
            }
        }
        final long identity = this.identities.incrementAndGet();
        final Requirement created = new RqFake(identity, input);
        this.items.put(identity, created);
        return created;
    }

    @Override
    public Requirement requirement(final long id) throws Exception {
        final Requirement selected = this.items.get(id);
        if (selected == null) {
            throw new RequirementNotFoundException(
                String.format("Requirement with id=%d not found", id)
            );
        }
        return selected;
    }

    @Override
    public Requirements selection(
        final Filters filters,
        final Page page,
        final Sorts sorts
    ) throws Exception {
        final List<Map.Entry<Requirement, ObjectNode>> selected =
            new ArrayList<>(this.items.size());
        for (final Requirement requirement : this.items.values()) {
            final ObjectNode fields = requirement.print(new MdJson()).print();
            boolean accepted = true;
            for (final Filter filter : filters.filters()) {
                final String value = filter.value();
                if (value != null && !value.isBlank()) {
                    accepted = switch (filter.name()) {
                        case "grade" -> fields.path("grade").asInt()
                            == Integer.parseInt(value.trim());
                        case "subject", "subjectName" -> fields
                            .path("subjectName").asText()
                            .toLowerCase(Locale.ROOT).contains(
                                value.trim().toLowerCase(Locale.ROOT)
                            );
                        case "part", "partType" -> fields.path("partType")
                            .asText().equals(
                                Requirement.PartType.valueOf(value.trim()).name()
                            );
                        default -> accepted;
                    };
                }
                if (!accepted) {
                    break;
                }
            }
            if (accepted) {
                selected.add(Map.entry(requirement, fields));
            }
        }
        Comparator<Map.Entry<Requirement, ObjectNode>> comparator = null;
        for (final String name : List.of("grade", "subject", "hours", "part")) {
            for (final Sort sort : sorts.sorts()) {
                if (
                    name.equals(sort.name())
                        && sort.direction() != null
                        && sort.direction() != Sort.Direction.NONE
                ) {
                    Comparator<Map.Entry<Requirement, ObjectNode>> next =
                        switch (name) {
                            case "grade" -> Comparator.comparingInt(
                                item -> item.getValue().path("grade").asInt()
                            );
                            case "subject" -> Comparator.comparing(
                                item -> item.getValue().path("subjectName").asText()
                            );
                            case "hours" -> Comparator.comparingInt(
                                item -> item.getValue().path("weeklyHours").asInt()
                            );
                            default -> Comparator.comparing(
                                item -> item.getValue().path("partType").asText()
                            );
                        };
                    if (sort.direction() == Sort.Direction.DESC) {
                        next = next.reversed();
                    }
                    if (comparator == null) {
                        comparator = next;
                    } else {
                        comparator = comparator.thenComparing(next);
                    }
                }
            }
        }
        if (comparator == null) {
            comparator = Comparator
                .<Map.Entry<Requirement, ObjectNode>>comparingInt(
                item -> item.getValue().path("grade").asInt()
            ).thenComparing(
                item -> item.getValue().path("subjectName").asText()
            ).thenComparingInt(
                item -> item.getValue().path("weeklyHours").asInt()
            ).thenComparing(
                item -> item.getValue().path("partType").asText()
            );
        }
        selected.sort(comparator);
        final int first = Math.max(0, (page.offset() - 1) * page.limit());
        final int last = Math.min(first + page.limit(), selected.size());
        final List<Requirement> pageitems = new ArrayList<>(page.limit());
        if (first < selected.size()) {
            for (int idx = first; idx < last; ++idx) {
                pageitems.add(selected.get(idx).getKey());
            }
        }
        return new RqsSelected(
            this,
            pageitems,
            new ResponsePage(page, selected.size())
        );
    }

    @Override
    public Iterable<Requirement> iterate() {
        return List.copyOf(this.items.values());
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return media.with(new PrItems("requirement", this.iterate()));
    }

    @Override
    public void remove(final long id) throws Exception {
        if (this.items.remove(id) == null) {
            throw new RequirementNotFoundException(
                String.format("Requirement with id=%d not found", id)
            );
        }
    }
}
