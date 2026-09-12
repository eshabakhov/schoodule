/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.page;

import com.eshabakhov.schoodule.Page;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link Page} from request parameters.
 *
 * @since 0.0.1
 */
@Component
public final class PageArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return Page.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer container,
        final NativeWebRequest request,
        final WebDataBinderFactory factory
    ) {
        final PageDefaults defaults = parameter.getParameterAnnotation(PageDefaults.class);
        final int deflim;
        final int defoff;
        if (defaults == null) {
            deflim = 15;
            defoff = 1;
        } else {
            deflim = defaults.limit();
            defoff = defaults.offset();
        }
        final String rawlim = request.getParameter("limit");
        final String rawoff = request.getParameter("offset");
        final int limit;
        final int offset;
        if (rawlim == null || rawlim.isBlank()) {
            limit = deflim;
        } else {
            limit = Integer.parseInt(rawlim);
        }
        if (rawoff == null || rawoff.isBlank()) {
            offset = defoff;
        } else {
            offset = Integer.parseInt(rawoff);
        }
        return new PageRequest(limit, offset);
    }
}
