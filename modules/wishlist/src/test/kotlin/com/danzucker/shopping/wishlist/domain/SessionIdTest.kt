package com.danzucker.shopping.wishlist.domain

import com.danzucker.shopping.common.core.InvalidInputException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class SessionIdTest {
    @Test
    fun `a UUID is a valid session id`() {
        SessionId.parse("3f2b8c1e-9a4d-4e6f-8b2a-1c3d5e7f9a0b").value shouldBe "3f2b8c1e-9a4d-4e6f-8b2a-1c3d5e7f9a0b"
    }

    @Test
    fun `a missing session id says which header to send`() {
        shouldThrow<InvalidInputException> { SessionId.parse(null) }.code shouldBe "MISSING_SESSION_ID"
        shouldThrow<InvalidInputException> { SessionId.parse("  ") }.code shouldBe "MISSING_SESSION_ID"
    }

    @Test
    fun `characters outside the safe alphabet are rejected`() {
        shouldThrow<InvalidInputException> { SessionId.parse("alice\nINFO forged log line") }.code shouldBe "INVALID_SESSION_ID"
    }

    @Test
    fun `an id longer than 64 characters is rejected`() {
        shouldThrow<InvalidInputException> { SessionId.parse("a".repeat(65)) }.code shouldBe "INVALID_SESSION_ID"
    }
}
