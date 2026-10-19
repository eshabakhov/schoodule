/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Login HTML controller.
 *
 * @since 0.0.1
 */
@Controller
public final class LoginPage {

    /**
     * New login page.
     */
    public LoginPage() {
        // Intentionally empty.
    }

    /**
     * Login method.
     *
     * @return Login
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @GetMapping("/users/login")
    public String login() {
        return "user/login";
    }
}
