/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.page;

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
     * Ctor.
     *
     * @param page Page argument resolver
     * @since 0.0.1
     */
    public PageWebMvcConfig(final PageArgumentResolver page) {
        this.page = page;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(this.page);
    }
}
