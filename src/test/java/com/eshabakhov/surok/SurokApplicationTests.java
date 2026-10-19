/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Entrypoint for Surok tests.
 *
 * @since 0.0.1
 */
final class SurokApplicationTests {

    // @checkstyle NonStaticMethodCheck (2 lines)
    @Test
    void contextLoads() {
        Assertions.assertNotNull(SurokApplication.class);
    }
}
