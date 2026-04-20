/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import java.util.List;

/**
 * Subscription plans available to personal (non-corporate) users.
 *
 * <p>Corporate users (TEACHER, DIRECTOR, DEPUTY_DIRECTOR, STUDENT)
 * belong to a school and receive their roles through the school
 * administrator — they never use subscriptions.</p>
 *
 * <p>The roles each plan grants are stored in {@code subscription_plan_roles}.
 * Changing a plan's role set requires only a database update, no code change.</p>
 *
 * @since 0.0.1
 */
public enum SubscriptionPlan {

    /** Free perpetual plan assigned to every new personal user. */
    BASIC(
        "Базовый",
        "Бесплатно",
        null,
        List.of(
            "Просмотр расписания школы",
            "Доступ к учебному плану классов",
            "До 1 расписания"
        )
    ),

    /** Paid plan with editing capabilities. */
    ADVANCED(
        "Продвинутый",
        "0 ₽ / месяц",
        0,
        List.of(
            "Всё из Базового",
            "Создание и редактирование расписаний",
            "Управление назначениями учителей",
            "До 5 расписаний",
            "Экспорт в PDF (скоро)"
        )
    ),

    /** Full-access plan with reporting and validation. */
    PRO(
        "Профессиональный",
        "0 ₽ / месяц",
        0,
        List.of(
            "Всё из Продвинутого",
            "Неограниченное количество расписаний",
            "Проверка и валидация плана",
            "Нагрузка учителей и отчёты",
            "Приоритетная поддержка",
            "API-доступ (скоро)"
        )
    ),

    /** Read-only plan for teachers and parents. */
    VIEWER(
        "Просмотрщик",
        "0 ₽ / месяц",
        0,
        List.of(
            "Просмотр готового расписания",
            "Фильтрация по классу и учителю",
            "Уведомления об изменениях (скоро)"
        )
    );

    /** Label. */
    private final String lbl;

    /** Price. */
    private final String prc;

    /** Monthly. */
    private final Integer mnth;

    /** Features. */
    private final List<String> feats;

    // @checkstyle ParameterNumberCheck (2 lines)
    SubscriptionPlan(
        final String label,
        final String price,
        final Integer monthly,
        final List<String> features
    ) {
        this.lbl = label;
        this.prc = price;
        this.mnth = monthly;
        this.feats = features;
    }

    /**
     * Human-readable plan name for UI display.
     *
     * @return Display label
     */
    public String label() {
        return this.lbl;
    }

    /**
     * Formatted price string for UI display.
     *
     * @return Price label
     */
    public String price() {
        return this.prc;
    }

    /**
     * Monthly price in rubles, or null for free plans.
     *
     * @return Monthly price or null
     */
    public Integer monthly() {
        return this.mnth;
    }

    /**
     * Feature list for UI display.
     *
     * @return Feature descriptions
     */
    public List<String> features() {
        return this.feats;
    }

    /**
     * Whether this plan is free (no expiry needed).
     *
     * @return True for BASIC
     */
    public boolean free() {
        return this.mnth == null;
    }
}
