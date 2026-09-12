/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.filter;

import com.eshabakhov.schoodule.Filter;
import com.eshabakhov.schoodule.Filters;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link Filters} from request parameters.
 *
 * @since 0.0.1
 */
@Component
public final class FiltersArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return Filters.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer container,
        final NativeWebRequest request,
        final WebDataBinderFactory factory
    ) {
        final Set<Filter> found = new LinkedHashSet<>(0);
        final String[] raw = request.getParameterValues("filter");
        if (raw != null) {
            for (final String item : raw) {
                if (item == null || item.isBlank()) {
                    continue;
                }
                final String[] parts = item.split(":", 2);
                if (parts.length > 1) {
                    found.add(new FilterRequest(parts[0], parts[1]));
                }
            }
        }
        return new FiltersRequest(found);
    }
}
