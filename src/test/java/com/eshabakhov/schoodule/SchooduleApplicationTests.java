/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint for Schoodule tests.
 *
 * @since 0.0.1
 */
final class SchooduleApplicationTests {

    /**
     * Logger used to emit info message for starting tests.
     */
    private final Logger logger;

    SchooduleApplicationTests() {
        this.logger = LoggerFactory.getLogger(SchooduleApplicationTests.class);
    }

    @Test
    void contextLoads() {
        this.logger.info("Starting Schoodule Application Tests");
    }

}
