/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.requirement.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.requirement.RequirementInput;
import org.springframework.util.MultiValueMap;

/**
 * Federal curriculum requirement input read from HTTP request parameters.
 *
 * @since 0.0.1
 */
public final class RqInputHttp implements RequirementInput {

    /** HTTP request parameters. */
    private final MultiValueMap<String, String> parameters;

    /**
     * New HTTP requirement input.
     *
     * @param parameters HTTP request parameters
     */
    public RqInputHttp(final MultiValueMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return new RqInputPart(
            new RqInputHours(
                new RqInputSubject(
                    new RqInputGrade(
                        new RqInput(),
                        this.parameters.getFirst("grade")
                    ),
                    this.parameters.getFirst("subjectName")
                ),
                this.parameters.getFirst("weeklyHours")
            ),
            this.parameters.getFirst("partType")
        ).print(media);
    }
}
