/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.config;

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

    /** Argument resolvers. */
    private final List<HandlerMethodArgumentResolver> arguments;

    /**
     * Ctor.
     *
     * @param arguments Argument resolvers
     * @since 0.0.1
     */
    public WebMvcConfig(final List<HandlerMethodArgumentResolver> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addAll(this.arguments);
    }
}
