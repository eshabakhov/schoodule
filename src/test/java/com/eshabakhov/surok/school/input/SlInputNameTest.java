/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.school.input;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * School names are validated and normalized before persistence.
 *
 * @since 0.0.1
 */
final class SlInputNameTest {

    /** Checks Unicode names are normalized. */
    @Test
    void trimsUnicodeSchoolNames() throws Exception {
        MatcherAssert.assertThat(
            "School name retained surrounding whitespace",
            new SlInputName(new SlInput(), "  Лицей №42 O'Brien  ")
                .print(new MdSlInput()).print().get("name"),
            Matchers.equalTo("Лицей №42 O'Brien")
        );
    }

    /**
     * Rejects missing or blank school names.
     *
     * @param name Invalid school name
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", " "})
    void rejectsMissingSchoolNames(final String name) {
        Assertions.assertThrows(
            SchoolInputException.class,
            () -> new SlInputName(new SlInput(), name).print(new MdSlInput()),
            "School accepted an empty name"
        );
    }
}
