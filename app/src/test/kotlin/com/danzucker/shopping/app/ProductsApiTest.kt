package com.danzucker.shopping.app

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.assertj.MockMvcTester

/**
 * The product endpoints over HTTP, through the full application context: routing, parameter
 * binding, the suspend controllers and the error mapping, against the real seed data.
 */
@SpringBootTest
@AutoConfigureMockMvc
internal class ProductsApiTest {
    @Autowired
    lateinit var mvc: MockMvcTester

    private fun get(uri: String) = mvc.call("GET", uri)

    @Test
    fun `the list returns the first page of 20 by default with paging metadata`() {
        val result = get("/products")

        result.status shouldBe 200
        result.json<Int>("$.page") shouldBe 1
        result.json<Int>("$.size") shouldBe 20
        result.json<Int>("$.totalItems") shouldBe 12
        result.json<Int>("$.totalPages") shouldBe 1
        result.json<List<Any>>("$.items").size shouldBe 12
    }

    @Test
    fun `product details carry every product field`() {
        val keys = get("/products/1").json<Map<String, Any>>("$").keys

        keys shouldBe DETAIL_FIELDS
    }

    @Test
    fun `list items are summaries without brand or category`() {
        get("/products").json<Map<String, Any>>("$.items[0]").keys shouldBe SUMMARY_FIELDS
    }

    @Test
    fun `search results are summaries too`() {
        get("/products/search?query=wireless").json<Map<String, Any>>("$.items[0]").keys shouldBe SUMMARY_FIELDS
    }

    @Test
    fun `brand and category still filter the list even though they are not returned`() {
        get("/products?brand=sony").json<List<String>>("$.items[*].name") shouldBe listOf("WH-1000XM5 Headphones")
        get("/products?category=books").json<List<String>>("$.items[*].name") shouldBe listOf("Kindle Paperwhite")
    }

    @Test
    fun `page and size cut the list`() {
        val result = get("/products?page=3&size=5")

        result.json<List<Int>>("$.items[*].id") shouldBe listOf(11, 12)
        result.json<Int>("$.totalPages") shouldBe 3
    }

    @Test
    fun `filters combine, brand ignores case, and the price range is inclusive`() {
        get("/products?brand=apple").json<List<Int>>("$.items[*].id") shouldBe listOf(1, 3)
        get("/products?category=Electronics&price_max=400").json<List<Int>>("$.items[*].id") shouldBe listOf(1, 2)
        get("/products?price_min=129.00&price_max=129.99").json<List<Int>>("$.items[*].id") shouldBe listOf(5, 10)
    }

    @Test
    fun `search matches name or description partially and takes filters too`() {
        get("/products/search?query=WIRELESS").json<List<Int>>("$.items[*].id") shouldBe listOf(1, 2)
        get("/products/search?query=running&category=Footwear").json<List<Int>>("$.items[*].id") shouldBe listOf(5, 6)
        get("/products/search?query=running&brand=adidas").json<List<Int>>("$.items[*].id") shouldBe listOf(6)
    }

    @Test
    fun `an unknown product is 404 with a problem body`() {
        val result = get("/products/999")

        result.status shouldBe 404
        result.response.contentType shouldBe "application/problem+json"
        result.json<String>("$.code") shouldBe "PRODUCT_NOT_FOUND"
        result.json<String>("$.instance") shouldBe "/products/999"
    }

    @Test
    fun `a non-numeric id is 400 naming the parameter`() {
        val result = get("/products/abc")

        result.status shouldBe 400
        result.json<String>("$.code") shouldBe "INVALID_PARAMETER"
        result.json<String>("$.errors[0].field") shouldBe "id"
    }

    @Test
    fun `invalid filters and paging are 400 with a specific code`() {
        get("/products?price_min=50&price_max=10").json<String>("$.code") shouldBe "INVALID_PRICE_RANGE"
        get("/products?price_min=cheap").json<String>("$.detail") shouldBe "price_min must be a number, e.g. 19.99; got 'cheap'."
        get("/products?size=500").json<String>("$.code") shouldBe "INVALID_PAGE_SIZE"
        get("/products?page=0").json<String>("$.code") shouldBe "INVALID_PAGE"
        get("/products/search").json<String>("$.code") shouldBe "MISSING_SEARCH_QUERY"
        get("/products/search?query=").status shouldBe 400
    }

    @Test
    fun `an unknown route is 404 in the same error shape`() {
        get("/nope").json<String>("$.code") shouldBe "ROUTE_NOT_FOUND"
    }

    private companion object {
        val SUMMARY_FIELDS = setOf("id", "name", "description", "imageUrl", "price")
        val DETAIL_FIELDS = SUMMARY_FIELDS + setOf("brand", "category")
    }
}
