/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.sort;

import com.eshabakhov.schoodule.Sort;
import com.eshabakhov.schoodule.Sorts;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link Sorts} from request parameters.
 *
 * @since 0.0.1
 */
@Component
public final class SortsArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Default direction.
     */
    private final Sort.Direction none = Sort.Direction.NONE;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return Sorts.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer container,
        final NativeWebRequest request,
        final WebDataBinderFactory factory
    ) {
        final List<Sort> found = new ArrayList<>(0);
        final String[] raw = request.getParameterValues("sort");
        if (raw != null) {
            found.addAll(this.requested(raw));
        }
        return new SortsRequest(found);
    }

    /**
     * Request sorts.
     *
     * @param raw Raw sort parameters
     * @return Sorts
     */
    private List<Sort> requested(final String... raw) {
        final List<Sort> found = new ArrayList<>(raw.length);
        for (final String item : raw) {
            if (item != null && !item.isBlank()) {
                final String[] parts = item.split("[:,]", 2);
                found.add(new SortRequest(parts[0], this.direction(parts)));
            }
        }
        return found;
    }

    /**
     * Direction from split sort value.
     *
     * @param parts Split sort value
     * @return Direction
     */
    private Sort.Direction direction(final String... parts) {
        final Sort.Direction direction;
        if (parts.length < 2) {
            direction = this.none;
        } else {
            direction = this.direction(parts[1]);
        }
        return direction;
    }

    /**
     * Direction from raw value.
     *
     * @param raw Raw direction
     * @return Direction
     */
    private Sort.Direction direction(final String raw) {
        final Sort.Direction direction;
        if (raw == null || raw.isBlank()) {
            direction = this.none;
        } else {
            direction = Sort.Direction.valueOf(raw.toUpperCase(Locale.ROOT));
        }
        return direction;
    }
}
