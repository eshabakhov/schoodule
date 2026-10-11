/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.header;

import com.eshabakhov.schoodule.Header;
import com.eshabakhov.schoodule.Media;
import com.eshabakhov.schoodule.media.MdThymeleaf;
import org.springframework.web.servlet.ModelAndView;

/**
 * HTML representation header.
 *
 * @since 0.0.1
 */
public final class HrHtml implements Header<ModelAndView> {

    /**
     * New HTML header.
     */
    public HrHtml() {
        // Intentionally empty.
    }

    @Override
    public Media<ModelAndView> media(
        final String template,
        final String attribute
    ) {
        return new MdThymeleaf(template, attribute);
    }
}
