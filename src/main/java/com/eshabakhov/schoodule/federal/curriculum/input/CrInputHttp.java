/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.federal.curriculum.input;

import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.federal.curriculum.CurriculumInput;
import org.springframework.util.MultiValueMap;

/**
 * Federal curriculum input read from HTTP request parameters.
 *
 * @since 0.0.1
 */
public final class CrInputHttp implements CurriculumInput {

    /** HTTP request parameters. */
    private final MultiValueMap<String, String> parameters;

    /**
     * New HTTP curriculum input.
     *
     * @param parameters HTTP request parameters
     */
    public CrInputHttp(final MultiValueMap<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public <T> Media<T> print(final Media<T> media) throws Exception {
        return new CrInputDescription(
            new CrInputYear(
                new CrInputVersion(
                    new CrInputWeek(
                        new CrInputLevel(
                            new CrInputTitle(
                                new CrInput(),
                                this.parameters.getFirst("title")
                            ),
                            this.parameters.getFirst("level")
                        ),
                        this.parameters.getFirst("week")
                    ),
                    this.parameters.getFirst("version")
                ),
                this.parameters.getFirst("year")
            ),
            this.parameters.getFirst("description")
        ).print(media);
    }
}
