/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.User;
import com.eshabakhov.schoodule.Users;
import com.eshabakhov.schoodule.tables.LoginAttempt;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.jooq.DSLContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * PostgreSQL users implementation.
 *
 * @since 0.0.1
 */
public final class UrsPostgres implements Users {

    /** JOOQ Table for User. */
    private static final com.eshabakhov.schoodule.tables.User USER =
        com.eshabakhov.schoodule.tables.User.USER;

    /** Database context. */
    private final DSLContext ctx;

    /** School ID. */
    private Long sid;

    public UrsPostgres(final DSLContext ctx) {
        this(ctx, null);
    }

    public UrsPostgres(final DSLContext ctx, final Long sid) {
        this.ctx = ctx;
        this.sid = sid;
    }

    @Override
    public User register(
        final String username,
        final String password,
        final String email,
        final Boolean corporate
    ) throws Exception {
        validEmail(email);
        validPassword(password);
        return this.ctx.transactionResult(
            conf -> {
                final DSLContext ttx = conf.dsl();
                final var inserted = ttx.insertInto(UrsPostgres.USER)
                    .set(UrsPostgres.USER.USERNAME, username)
                    .set(UrsPostgres.USER.PASSWORD, new BCryptPasswordEncoder().encode(password))
                    .set(UrsPostgres.USER.EMAIL, email)
                    .set(UrsPostgres.USER.CORPORATE, corporate)
                    .set(UrsPostgres.USER.SCHOOL_ID, this.sid)
                    .returning()
                    .fetchOne();
                if (inserted == null) {
                    throw new UserCreationException("Failed to create user");
                }
                final var user = new UrPostgres(ttx, inserted.getId());
                user.roles().grant(RoleEnum.STUDENT);
                user.roles().grant(RoleEnum.BASIC_MAKER);
                return user;
            }
        );
    }

    @Override
    public User identification(final String login) throws Exception {
        final var selected = this.ctx.selectFrom(UrsPostgres.USER)
            .where(
                UrsPostgres.USER.DELETED.isNull().and(
                    UrsPostgres.USER.USERNAME.eq(login)
                        .or(UrsPostgres.USER.EMAIL.eq(login))
                )
            )
            .fetchOne();
        if (selected == null) {
            throw new UserNotFoundException(String.format("User with login '%s' not found", login));
        }
        return new UrPostgres(this.ctx, selected.getId());
    }

    @Override
    public User identification(final Long uid) throws Exception {
        final var selected = this.ctx.selectFrom(UrsPostgres.USER)
            .where(UrsPostgres.USER.ID.eq(uid).and(UrsPostgres.USER.DELETED.isNull()))
            .fetchOne();
        if (selected == null) {
            throw new UserNotFoundException(String.format("User with id '%s' not found", uid));
        }
        return new UrPostgres(this.ctx, selected.getId());
    }

    @Override
    public User authentication(final String login) throws Exception {
        final var user = this.identification(login);
        final var time = Instant.now().atOffset(ZoneOffset.UTC);
        final var selected = this.ctx.selectFrom(LoginAttempt.LOGIN_ATTEMPT)
            .where(
                LoginAttempt.LOGIN_ATTEMPT.USER_ID.eq(user.uid())
                    .and(
                        LoginAttempt.LOGIN_ATTEMPT.TIME.greaterOrEqual(
                            time.minus(Duration.ofMinutes(5))
                        )
                    )
                    .and(LoginAttempt.LOGIN_ATTEMPT.TIME.lessOrEqual(time))
                    .and(LoginAttempt.LOGIN_ATTEMPT.SUCCESS.eq(false))
            )
            .fetch();
        if (selected.size() > 5) {
            throw new UserLockedException(String.format("User %s is locked", login));
        }
        return user;
    }

    @Override
    public void remove(final Long uid) throws Exception {
        this.ctx.update(UrsPostgres.USER)
            .set(UrsPostgres.USER.DELETED, Instant.now().atOffset(ZoneOffset.UTC))
            .where(UrsPostgres.USER.ID.eq(uid))
            .execute();
    }

    private static void validEmail(final String email) throws UserCreationException {
        if (email == null) {
            throw new UserCreationException("Получен пустой email");
        }
        if (!email.matches("^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            throw new UserCreationException("Email введен некорректно");
        }
    }

    private static void validPassword(final String password) throws UserCreationException {
        if (password == null) {
            throw new UserCreationException("Получен пустой пароль");
        }
        if (password.length() < 8) {
            throw new UserCreationException("Длина пароля меньше 8 символов");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new UserCreationException("Пароль должен содержать хотя бы одну цифру");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new UserCreationException("Пароль должен содержать хотя бы одну строчную букву");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new UserCreationException("Пароль должен содержать хотя бы одну заглавную букву");
        }
        if (password.chars().noneMatch(
            c -> "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?".indexOf(c) >= 0
        )) {
            throw new UserCreationException("Пароль должен содержать хотя бы один спец. символ");
        }
    }

    /**
     * User creation exception.
     *
     * @since 0.0.1
     */
    public static class UserCreationException extends Exception {

        public UserCreationException(final String message) {
            super(message);
        }
    }

    /**
     * User not found exception.
     *
     * @since 0.0.1
     */
    public static class UserNotFoundException extends Exception {

        public UserNotFoundException(final String message) {
            super(message);
        }
    }

    /**
     * User locked exception.
     *
     * @since 0.0.1
     */
    public static class UserLockedException extends Exception {

        public UserLockedException(final String message) {
            super(message);
        }
    }
}
