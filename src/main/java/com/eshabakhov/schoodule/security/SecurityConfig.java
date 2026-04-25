/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring security config.
 *
 * @since 0.0.1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** HSTS max age in seconds. */
    private static final long HSTS_AGE = 31_536_000L;

    /** Permissions policy. */
    private static final String PERMISSIONS = String.join(
        ", ",
        "accelerometer=()",
        "camera=()",
        "geolocation=()",
        "gyroscope=()",
        "microphone=()",
        "payment=()",
        "usb=()"
    );

    /** User details service. */
    private final DatabaseUserDetailsService userdetails;

    /** Login success handler. */
    private final LoginSuccessHandler success;

    /** Login failure handler. */
    private final LoginFailureHandler failure;

    /** OAuth2 login success handler. */
    private final OAuthSuccessHandler oauthhandler;

    /** OAuth2 user service that resolves Yandex principal to AuthUser. */
    private final YandexOAuth2UserService oauthservice;

    /** Whether API docs are enabled. */
    @Value("${app.security.docs.enabled:false}")
    private boolean docs;

    /** Whether prometheus endpoint is enabled. */
    @Value("${app.security.prometheus.enabled:false}")
    private boolean prom;

    /** Allowed CORS origins. */
    @Value("${app.security.allowed-origins:}")
    private String origins;

    /**
     * Security filter chain configuration.
     *
     * @param http HttpSecurity to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(final HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/api/**")
            .cors(Customizer.withDefaults())
            .csrf(
                csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .exceptionHandling(
                exceptions -> exceptions
                    .authenticationEntryPoint(
                        (request, response, auth) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                    )
                    .accessDeniedHandler(
                        (request, response, denied) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN)
                    )
            )
            .httpBasic(Customizer.withDefaults())
            .userDetailsService(this.userdetails)
            .build();
    }

    /**
     * Security filter chain configuration.
     *
     * @param http HttpSecurity to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(final HttpSecurity http) throws Exception {
        return http
            .cors(Customizer.withDefaults())
            .csrf(
                csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(
                auth -> {
                    auth.requestMatchers(
                        "/users/login",
                        "/users/register",
                        "/favicon.svg",
                        "/css/**",
                        "/js/**",
                        "/"
                    ).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    if (this.docs) {
                        auth.requestMatchers(
                            "/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**"
                        ).hasRole("ADMIN");
                    } else {
                        auth.requestMatchers(
                            "/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**"
                        ).denyAll();
                    }
                    if (this.prom) {
                        auth.requestMatchers("/actuator/prometheus").permitAll();
                    } else {
                        auth.requestMatchers("/actuator/prometheus").denyAll();
                    }
                    auth.anyRequest().authenticated();
                }
            )
            .headers(
                headers -> headers
                    .referrerPolicy(
                        policy -> policy.policy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy
                                .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                        )
                    )
                    .frameOptions(frame -> frame.deny())
                    .httpStrictTransportSecurity(
                        hsts -> hsts
                            .includeSubDomains(true)
                            .preload(true)
                            .maxAgeInSeconds(SecurityConfig.HSTS_AGE)
                    )
                    .permissionsPolicy(
                        permissions -> permissions.policy(SecurityConfig.PERMISSIONS)
                    )
            )
            .formLogin(
                form -> form
                    .loginPage("/users/login")
                    .loginProcessingUrl("/login")
                    .successHandler(this.success)
                    .failureHandler(this.failure)
                    .permitAll()
            )
            .oauth2Login(
                oauth -> oauth
                    .loginPage("/users/login")
                    .userInfoEndpoint(
                        endpoint -> endpoint.userService(this.oauthservice)
                    )
                    .successHandler(this.oauthhandler)
            )
            .logout(
                logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/users/login?logout")
            )
            .userDetailsService(this.userdetails)
            .exceptionHandling(
                exceptions -> exceptions
                    .accessDeniedHandler(
                        (request, response, denied) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN)
                    )
            )
            .build();
    }

    /**
     * CORS configuration source.
     *
     * @return Configured CorsConfigurationSource
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(
            List.of("Authorization", "Content-Type", "X-CSRF-TOKEN", "version")
        );
        final List<String> allowed = Arrays.stream(this.origins.split(","))
            .map(String::trim)
            .filter(origin -> !origin.isBlank())
            .toList();
        if (!allowed.isEmpty()) {
            config.setAllowedOrigins(allowed);
        }
        config.setAllowCredentials(!allowed.isEmpty());
        final var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * PasswordEncoder.
     *
     * @return PasswordEncoder
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * OpenAPI definition with basic authentication support.
     *
     * @return OpenAPI definition
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Schoodule API")
                    .version("0.0.1")
            )
            .components(
                new Components()
                    .addSecuritySchemes(
                        "Basic auth",
                        new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                    )
            )
            .addSecurityItem(new SecurityRequirement().addList("Basic auth"));
    }

    /**
     * AuthenticationProvider.
     *
     * @param service DatabaseUserDetailsService
     * @param encoder PasswordEncoder
     * @return DaoAuthenticationProvider
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @Bean
    DaoAuthenticationProvider authenticationProvider(
        final DatabaseUserDetailsService service,
        final PasswordEncoder encoder
    ) {
        final var provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(encoder);
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }
}
