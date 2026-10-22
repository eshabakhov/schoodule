/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import com.eshabakhov.surok.media.MdThymeleaf;
import com.eshabakhov.surok.school.SlWeb;
import com.eshabakhov.surok.school.SlsPostgres;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for user profile page.
 *
 * @since 0.0.1
 */
@Controller
@RequestMapping("/users/profile")
public final class ProfilePage {

    /** Database context. */
    private final DSLContext ctx;

    ProfilePage(final DSLContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Show user profile.
     *
     * @param user Authenticated user
     * @param model View model
     * @return Profile view
     * @throws Exception If school loading fails
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String profile(
        @AuthenticationPrincipal final AuthUser user,
        final Model model
    ) throws Exception {
        model.addAttribute("pageTitle", "Профиль");
        model.addAttribute("user", user);
        if (user.info().school() != null) {
            model.addAttribute(
                "school", new SlWeb(new SlsPostgres(this.ctx).school(user.info().school()))
                    .print(new MdThymeleaf("", "")).print().getModel()
            );
        } else {
            model.addAttribute("school", null);
        }
        return "user/profile";
    }
}
