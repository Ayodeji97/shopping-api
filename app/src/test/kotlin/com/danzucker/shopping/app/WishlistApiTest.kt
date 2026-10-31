package com.danzucker.shopping.app

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.assertj.MockMvcTester

/** The wishlist endpoints over HTTP, through the full application context. */
@SpringBootTest
@AutoConfigureMockMvc
internal class WishlistApiTest {
    @Autowired
    lateinit var mvc: MockMvcTester

    private val session = newSession()

    @Test
    fun `a new session has an empty wishlist`() {
        val result = mvc.call("GET", "/wishlist", session)

        result.status shouldBe 200
        result.json<List<Int>>("$.productIds") shouldBe emptyList()
    }

    @Test
    fun `add, list, conflict, remove and remove again`() {
        mvc.call("POST", "/wishlist/3", session).status shouldBe 201
        val added = mvc.call("POST", "/wishlist/1", session)
        added.status shouldBe 201
        added.json<List<Int>>("$.productIds") shouldBe listOf(3, 1)

        mvc.call("GET", "/wishlist", session).json<List<Int>>("$.productIds") shouldBe listOf(3, 1)

        val duplicate = mvc.call("POST", "/wishlist/3", session)
        duplicate.status shouldBe 409
        duplicate.json<String>("$.code") shouldBe "ALREADY_IN_WISHLIST"

        val removed = mvc.call("DELETE", "/wishlist/3", session)
        removed.status shouldBe 204
        removed.response.contentAsString shouldBe ""

        mvc.call("DELETE", "/wishlist/3", session).json<String>("$.code") shouldBe "NOT_IN_WISHLIST"
        mvc.call("GET", "/wishlist", session).json<List<Int>>("$.productIds") shouldBe listOf(1)
    }

    @Test
    fun `sessions are isolated`() {
        mvc.call("POST", "/wishlist/5", session)

        mvc.call("GET", "/wishlist", newSession()).json<List<Int>>("$.productIds") shouldBe emptyList()
    }

    @Test
    fun `adding a product that does not exist is 404`() {
        val result = mvc.call("POST", "/wishlist/999", session)

        result.status shouldBe 404
        result.json<String>("$.code") shouldBe "PRODUCT_NOT_FOUND"
    }

    @Test
    fun `a missing session header is 400 and says what to send`() {
        val result = mvc.call("GET", "/wishlist")

        result.status shouldBe 400
        result.json<String>("$.code") shouldBe "MISSING_SESSION_ID"
        result.json<String>("$.errors[0].field") shouldBe "X-Session-Id"
    }

    @Test
    fun `a malformed session header is 400`() {
        mvc.call("POST", "/wishlist/1", "not valid!").json<String>("$.code") shouldBe "INVALID_SESSION_ID"
    }
}
