/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.media;

import com.eshabakhov.schoodule.Media;
import java.util.Map;
import org.springframework.web.servlet.ModelAndView;

/**
 * Media rendered as a Spring MVC view.
 *
 * @since 0.0.1
 */
public interface ViewMedia extends Media {

    /**
     * Adds page model attributes.
     *
     * @param values Model attributes
     * @return Updated media
     */
    ViewMedia attributes(Map<String, Object> values);

    /**
     * Sets page title format applied to the printed title.
     *
     * @param format Page title format
     * @return Updated media
     */
    ViewMedia title(String format);

    /**
     * Returns collected values.
     *
     * @return Collected values
     */
    Map<String, Object> map();

    /**
     * Builds a view from collected values.
     *
     * @return Model and view
     */
    ModelAndView view();
}
