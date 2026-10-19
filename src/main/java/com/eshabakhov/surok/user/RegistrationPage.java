/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Registration HTML controller.
 *
 * @since 0.0.1
 */
@Controller
public final class RegistrationPage {

    /**
     * New registration page.
     */
    public RegistrationPage() {
        // Intentionally empty.
    }

    /**
     * Registration page.
     *
     * @return Registration view
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @GetMapping("/users/register")
    public String register() {
        return "user/register";
    }
}
