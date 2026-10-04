/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Entrypoint for Schoodule tests.
 *
 * @since 0.0.1
 */
final class SchooduleApplicationTests {

    // @checkstyle NonStaticMethodCheck (2 lines)
    @Test
    void contextLoads() {
        Assertions.assertNotNull(SchooduleApplication.class);
    }
}
