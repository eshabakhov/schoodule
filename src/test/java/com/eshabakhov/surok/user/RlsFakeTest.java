/*
 * © 2025-2026 Eset Shabakhov. Surok
 */
package com.eshabakhov.surok.user;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link RlsFake}.
 *
 * @since 0.0.1
 */
final class RlsFakeTest {

    @Test
    void grantsRoleWithNextIdentity() {
        MatcherAssert.assertThat(
            "Granted fake role must receive the next identity",
            new RlsFake(
                new ArrayList<>(0),
                new AtomicLong(3L)
            ).grant(Roles.RoleEnum.ADMIN).uid(),
            Matchers.is(4L)
        );
    }

    @Test
    void grantsRequestedRole() {
        MatcherAssert.assertThat(
            "Granted fake role must retain the requested name",
            new RlsFake(
                new ArrayList<>(0),
                new AtomicLong(0L)
            ).grant(Roles.RoleEnum.TEACHER).name(),
            Matchers.is("TEACHER")
        );
    }

    @Test
    void listsRoles() {
        final Role role = new RlFake(3L, "VIEWER");
        MatcherAssert.assertThat(
            "Fake roles must expose stored assignments",
            new RlsFake(
                new ArrayList<>(List.of(role)),
                new AtomicLong(3L)
            ).list(),
            Matchers.contains(Matchers.sameInstance(role))
        );
    }

    @Test
    void revokesRole() {
        final List<Role> memory = new ArrayList<>(
            List.of(new RlFake(3L, "VIEWER"))
        );
        new RlsFake(
            memory,
            new AtomicLong(3L)
        ).revoke(3L);
        MatcherAssert.assertThat(
            "Revoked role must be removed from fake memory",
            memory,
            Matchers.empty()
        );
    }

    @Test
    void ignoresMissingRoleRevocation() {
        final Role role = new RlFake(3L, "VIEWER");
        final List<Role> memory = new ArrayList<>(List.of(role));
        new RlsFake(
            memory,
            new AtomicLong(3L)
        ).revoke(99L);
        MatcherAssert.assertThat(
            "Unknown role revocation must preserve fake memory",
            memory,
            Matchers.contains(Matchers.sameInstance(role))
        );
    }

    @Test
    void startsEmpty() {
        MatcherAssert.assertThat(
            "Default fake roles must be empty",
            new RlsFake().list(),
            Matchers.empty()
        );
    }
}
