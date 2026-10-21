/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.building.input;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link BdInputName}.
 *
 * @since 0.0.1
 */
final class BdInputNameTest {

    @ParameterizedTest
    @ValueSource(strings = {" North ", "A"})
    void printsTrimmedName(final String name) throws Exception {
        MatcherAssert.assertThat(
            "Name must be printed without surrounding spaces",
            new BdInputName(new BdInput(), name)
                .print(new MdBdInput()).print(),
            Matchers.is(Map.of("name", name.trim()))
        );
    }

    @Test
    void rejectsNullName() {
        Assertions.assertThrows(
            BuildingInputException.class,
            () -> new BdInputName(new BdInput(), null)
                .print(new MdBdInput())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t"})
    void rejectsBlankName(final String name) {
        Assertions.assertThrows(
            BuildingInputException.class,
            () -> new BdInputName(new BdInput(), name)
                .print(new MdBdInput())
        );
    }
}
