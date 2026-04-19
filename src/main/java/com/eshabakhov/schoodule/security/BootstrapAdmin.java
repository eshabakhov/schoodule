/*
 * В© 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.security;

import com.eshabakhov.schoodule.enums.RoleType;
import com.eshabakhov.schoodule.tables.Role;
import com.eshabakhov.schoodule.tables.UserRole;
import com.eshabakhov.schoodule.user.RoleEnum;
import com.eshabakhov.schoodule.user.UrsPostgres;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin when explicitly enabled.
 *
 * @since 0.0.1
 */
@Component
public final class BootstrapAdmin implements ApplicationRunner {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapAdmin.class);

    /**
     * Database context.
     */
    private final DSLContext ctx;

    /**
     * Environment properties.
     */
    private final Environment env;

    /**
     * Ctor.
     *
     * @param ctx Database context
     * @param env Environment properties
     */
    public BootstrapAdmin(
        final DSLContext ctx,
        final Environment env
    ) {
        this.ctx = ctx;
        this.env = env;
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        if (this.bootstrapEnabled()) {
            if (this.hasActiveAdmin()) {
                BootstrapAdmin.LOG.info("Bootstrap admin skipped: active admin already exists");
            } else {
                this.validateConfig();
                final var users = new UrsPostgres(this.ctx);
                final var admin = users.register(
                    this.username(),
                    this.password(),
                    this.email(),
                    false
                );
                admin.roles().grant(RoleEnum.ADMIN);
                BootstrapAdmin.LOG.info(
                    "Bootstrap admin created for username '{}' and email '{}'",
                    this.username(),
                    this.email()
                );
            }
        }
    }

    /**
     * Validates bootstrap settings before creating an admin.
     */
    private void validateConfig() {
        if (this.username().isBlank()) {
            throw new IllegalStateException("Bootstrap admin username is required");
        }
        if (this.email().isBlank()) {
            throw new IllegalStateException("Bootstrap admin email is required");
        }
        if (this.password().isBlank()) {
            throw new IllegalStateException("Bootstrap admin password is required");
        }
    }

    /**
     * Whether bootstrap admin creation is enabled.
     *
     * @return True if enabled
     */
    private boolean bootstrapEnabled() {
        return Boolean.parseBoolean(this.env.getProperty("app.bootstrap-admin.enabled", "false"));
    }

    /**
     * Bootstrap admin username.
     *
     * @return Username
     */
    private String username() {
        return this.env.getProperty("app.bootstrap-admin.username", "");
    }

    /**
     * Bootstrap admin email.
     *
     * @return Email
     */
    private String email() {
        return this.env.getProperty("app.bootstrap-admin.email", "");
    }

    /**
     * Bootstrap admin password.
     *
     * @return Password
     */
    private String password() {
        return this.env.getProperty("app.bootstrap-admin.password", "");
    }

    /**
     * Checks whether any active admin exists.
     *
     * @return True if active admin already exists
     */
    private boolean hasActiveAdmin() {
        return this.ctx.fetchExists(
            this.ctx.selectOne()
                .from(UserRole.USER_ROLE)
                .join(Role.ROLE)
                .on(UserRole.USER_ROLE.ROLE_ID.eq(Role.ROLE.ID))
                .join(com.eshabakhov.schoodule.tables.User.USER)
                .on(UserRole.USER_ROLE.USER_ID.eq(com.eshabakhov.schoodule.tables.User.USER.ID))
                .where(
                    Role.ROLE.NAME.eq(RoleType.ADMIN)
                        .and(com.eshabakhov.schoodule.tables.User.USER.DELETED.isNull())
                )
        );
    }
}
