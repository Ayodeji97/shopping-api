package com.danzucker.shopping.app

import com.jayway.jsonpath.JsonPath
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.util.UUID

/** Reads one value from a JSON response body. */
internal fun <T> MvcTestResult.json(path: String): T = JsonPath.read(response.contentAsString, path)

internal val MvcTestResult.status: Int get() = response.status

/** A fresh session id, so tests sharing one application context never share a wishlist. */
internal fun newSession(): String = UUID.randomUUID().toString()

/**
 * `exchange()` waits for asynchronous handling to finish, which `suspend` controllers need: Spring
 * MVC runs them asynchronously.
 */
internal fun MockMvcTester.call(
    method: String,
    uri: String,
    session: String? = null,
): MvcTestResult {
    val builder =
        when (method) {
            "GET" -> get()
            "POST" -> post()
            "DELETE" -> delete()
            else -> error("Unsupported method $method")
        }.uri(uri)
    session?.let { builder.header("X-Session-Id", it) }
    return builder.exchange()
}
