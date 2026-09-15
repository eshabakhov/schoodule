/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.config;

import com.eshabakhov.schoodule.filter.FiltersArgumentResolver;
import com.eshabakhov.schoodule.page.PageArgumentResolver;
import com.eshabakhov.schoodule.sort.SortsArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 *
 * @since 0.0.1
 */
@Configuration(proxyBeanMethods = false)
public final class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Page argument resolver.
     */
    private final PageArgumentResolver page;

    /**
     * Filters argument resolver.
     */
    private final FiltersArgumentResolver filters;

    /**
     * Sorts argument resolver.
     */
    private final SortsArgumentResolver sorts;

    /**
     * Ctor.
     *
     * @param page Page argument resolver
     * @param filters Filters argument resolver
     * @param sorts Sorts argument resolver
     * @since 0.0.1
     */
    public WebMvcConfig(
        final PageArgumentResolver page,
        final FiltersArgumentResolver filters,
        final SortsArgumentResolver sorts
    ) {
        this.page = page;
        this.filters = filters;
        this.sorts = sorts;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this.page);
        resolvers.add(this.filters);
        resolvers.add(this.sorts);
    }
}
