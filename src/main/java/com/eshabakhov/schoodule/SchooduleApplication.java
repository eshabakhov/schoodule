/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entrypoint.
 *
 * @since 0.0.1
 */
@SuppressWarnings(
    {
        "PMD.ProhibitPublicStaticMethods",
        "PMD.UseUtilityClass",
        "PMD.UncommentedEmptyConstructor"
    }
)
@SpringBootApplication
public class SchooduleApplication {

    protected SchooduleApplication() { }

    public static void main(final String[] args) {
        SpringApplication.run(SchooduleApplication.class, args);
    }
}
