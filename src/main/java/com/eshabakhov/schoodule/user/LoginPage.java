/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *  Login HTML controller.
 *
 * @since 0.0.1
 */
@Controller
public final class LoginPage {

    /**
     * Login method.
     * @return Login
     * @checkstyle NonStaticMethodCheck (2 lines)
     */
    @GetMapping("/users/login")
    public String login() {
        return "user/login";
    }
}
