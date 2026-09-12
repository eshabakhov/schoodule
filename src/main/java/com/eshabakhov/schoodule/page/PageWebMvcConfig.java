/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.page;

import com.eshabakhov.schoodule.sort.SortsArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Pagination web MVC configuration.
 *
 * @since 0.0.1
 */
@Configuration(proxyBeanMethods = false)
public final class PageWebMvcConfig implements WebMvcConfigurer {

    /**
     * Page argument resolver.
     */
    private final PageArgumentResolver page;

    /**
     * Sorts argument resolver.
     */
    private final SortsArgumentResolver sorts;

    /**
     * Ctor.
     *
     * @param page Page argument resolver
     * @param sorts Sorts argument resolver
     * @since 0.0.1
     */
    public PageWebMvcConfig(
        final PageArgumentResolver page,
        final SortsArgumentResolver sorts
    ) {
        this.page = page;
        this.sorts = sorts;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this.page);
        resolvers.add(this.sorts);
    }
}
