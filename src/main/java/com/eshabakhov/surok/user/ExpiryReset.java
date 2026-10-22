/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import com.eshabakhov.surok.sql.ClasspathSql;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nightly job that resets expired personal-user subscriptions to BASIC
 * and downgrades their subscription-managed role sets accordingly.
 *
 * <p>Requires {@code @EnableScheduling} on the application class.</p>
 *
 * <p>Schedule: every day at 03:00 UTC.</p>
 * Usage example:
 * <pre>
 * // Triggered automatically by Spring Scheduler; no manual invocation needed.
 * </pre>
 *
 * @since 0.0.1
 */
@Component
public final class ExpiryReset {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ExpiryReset.class);

    /** Database context. */
    private final DSLContext ctx;

    /**
     * New expiry reset job.
     *
     * @param ctx Database context
     */
    public ExpiryReset(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Runs at 03:00 UTC every day and downgrades all expired subscriptions.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    public void reset() {
        final Result<Record> expired = this.ctx.resultQuery(
            new ClasspathSql("subscription/find-by-plan-and-expires-at-and-corporate.sql").text(),
            "BASIC",
            Instant.now().atOffset(ZoneOffset.UTC),
            false
        ).fetch();
        final int size;
        if (expired.isEmpty()) {
            size = 0;
        } else {
            final List<String> basic = this.ctx.resultQuery(
                new ClasspathSql("subscription/role/find-by-plan.sql").text(),
                Subscription.Plan.BASIC.name()
            ).fetch()
                .stream()
                .map(r -> r.get("role_name", String.class))
                .toList();
            for (final Record row : expired) {
                final long uid = row.get("user_id", Long.class);
                this.ctx.transaction(
                    conf -> {
                        final DSLContext dsl = conf.dsl();
                        ExpiryReset.swap(dsl, uid, basic);
                        dsl.resultQuery(
                            new ClasspathSql("subscription/reset.sql").text(),
                            "BASIC",
                            Instant.now().atOffset(ZoneOffset.UTC),
                            uid
                        ).execute();
                    }
                );
            }
            size = expired.size();
        }
        if (size > 0) {
            ExpiryReset.LOG.info(
                "[ExpiryReset] Downgraded {} expired subscription(s) to BASIC%n", size
            );
        }
    }

    private static void swap(
        final DSLContext dsl,
        final long user,
        final List<String> grants
    ) {
        dsl.execute(
            new ClasspathSql("user/role/delete-subscription-roles.sql").text(),
            user
        );
        for (final String role : grants) {
            final Record rid = dsl.resultQuery(
                new ClasspathSql("role/find-by-name.sql").text(),
                role
            ).fetchOne();
            if (rid == null) {
                throw new IllegalStateException(
                    String.format("Role not found in database (name=%s)", role)
                );
            }
            dsl.resultQuery(
                new ClasspathSql("user/role/create-if-absent.sql").text(),
                user,
                rid.get("id", Long.class)
            ).execute();
        }
    }
}
