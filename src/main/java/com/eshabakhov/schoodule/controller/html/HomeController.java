/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.controller.html;

import com.eshabakhov.schoodule.user.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for landing page.
 *
 * @since 0.0.1
 */
@Controller
public final class HomeController {

    // @checkstyle NonStaticMethodCheck (2 lines)
    @GetMapping("/")
    public String index(@AuthenticationPrincipal final AuthUser user, final Model model) {
        if (user == null) {
            model.addAttribute("url", "/users/login");
        } else {
            if (user.isAdmin()) {
                model.addAttribute("url", "/schools");
            } else {
                model.addAttribute(
                    "url",
                    String.format("/schools/%d", user.info().school())
                );
            }
        }
        return "index";
    }
}
