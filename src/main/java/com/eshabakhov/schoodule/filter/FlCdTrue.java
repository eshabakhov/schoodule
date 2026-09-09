/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.filter;

import org.jooq.Condition;
import org.jooq.impl.DSL;

/**
 * Filter that accepts all items.
 *
 * @since 0.0.1
 */
public final class FlCdTrue implements FlConditional {

    @Override
    public Condition condition() {
        return DSL.trueCondition();
    }
}
