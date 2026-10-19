/*
 * © 2025-2026 Eset Shabakhov. Schoodule
 */
package com.eshabakhov.schoodule.user;

import com.eshabakhov.schoodule.media.MdJson;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link UrFake}.
 *
 * @since 0.0.1
 */
final class UrFakeTest {

    @Test
    void printsAdministrator() throws Exception {
        MatcherAssert.assertThat(
            "Fake administrator must print its public representation",
            new UrFake(
                12L,
                new CdFake("admin", "secret"),
                new RlsFake(
                    List.of(new RlFake(1L, "ADMIN")),
                    new AtomicLong(1L)
                )
            ).print(new MdJson()).print(),
            Matchers.is(
                JsonNodeFactory.instance.objectNode()
                    .put("id", 12L)
                    .put("username", "admin")
                    .put("admin", true)
            )
        );
    }

    @Test
    void exposesIdentity() {
        MatcherAssert.assertThat(
            "Fake user must expose its identity",
            new UrFake(
                15L,
                new CdFake("teacher", "secret"),
                new RlsFake()
            ).uid(),
            Matchers.is(15L)
        );
    }

    @Test
    void exposesCredentials() {
        final Credentials credentials = new CdFake("teacher", "secret");
        MatcherAssert.assertThat(
            "Fake user must expose its credentials",
            new UrFake(15L, credentials, new RlsFake()).credentials(),
            Matchers.sameInstance(credentials)
        );
    }

    @Test
    void exposesRoles() {
        final Roles roles = new RlsFake();
        MatcherAssert.assertThat(
            "Fake user must expose its roles",
            new UrFake(
                15L, new CdFake("teacher", "secret"), roles
            ).roles(),
            Matchers.sameInstance(roles)
        );
    }

    @Test
    void exposesEmptySchool() {
        MatcherAssert.assertThat(
            "Fake user information must expose an empty school",
            new UrFake(
                15L,
                new CdFake("teacher", "secret"),
                new RlsFake()
            ).info().school(),
            Matchers.nullValue()
        );
    }

    @Test
    void exposesNonCorporateInformation() {
        MatcherAssert.assertThat(
            "Fake user information must be non-corporate",
            new UrFake(
                15L,
                new CdFake("teacher", "secret"),
                new RlsFake()
            ).info().corporate(),
            Matchers.is(false)
        );
    }

    @Test
    void exposesAliveInformation() {
        MatcherAssert.assertThat(
            "Fake user information must be alive",
            new UrFake(
                15L,
                new CdFake("teacher", "secret"),
                new RlsFake()
            ).info().alive(),
            Matchers.is(true)
        );
    }

    @Test
    void recognizesAdministrator() {
        MatcherAssert.assertThat(
            "Fake user with an admin role must be an administrator",
            new UrFake(
                15L,
                new CdFake("admin", "secret"),
                new RlsFake(
                    List.of(new RlFake(1L, "ADMIN")),
                    new AtomicLong(1L)
                )
            ).isAdmin(),
            Matchers.is(true)
        );
    }

    @Test
    void rejectsAdministratorWithoutRole() {
        MatcherAssert.assertThat(
            "Fake user without an admin role must not be an administrator",
            new UrFake(
                15L,
                new CdFake("teacher", "secret"),
                new RlsFake()
            ).isAdmin(),
            Matchers.is(false)
        );
    }
}
