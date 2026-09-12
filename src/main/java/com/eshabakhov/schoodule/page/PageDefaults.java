/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default pagination parameters.
 *
 * @since 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PageDefaults {

    /**
     * Default page size.
     *
     * @return Page size
     */
    int limit() default 15;

    /**
     * Default page number.
     *
     * @return Page number
     */
    int offset() default 1;
}
