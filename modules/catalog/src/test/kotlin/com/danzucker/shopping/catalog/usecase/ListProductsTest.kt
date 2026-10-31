package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.data.InMemoryProductRepository
import com.danzucker.shopping.catalog.domain.ProductQuery
import com.danzucker.shopping.catalog.product
import com.danzucker.shopping.common.core.InvalidInputException
import com.danzucker.shopping.common.core.PageRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * [ListProducts] against a real in-memory data source built from the test's own products, not a
 * mock: the test checks what the user gets back, not which calls were made.
 */
internal class ListProductsTest {
    private val catalog =
        listOf(
            product(3, brand = "Nike", category = "Footwear", price = "129.99"),
            product(1, brand = "Apple", category = "Electronics", price = "249.00"),
            product(2, brand = "Sony", category = "Electronics", price = "399.99"),
            product(4, brand = "Nike", category = "Apparel", price = "34.99"),
            product(5, brand = "Apple", category = "Electronics", price = "2499.00"),
        )
    private val listProducts = ListProducts(InMemoryProductRepository(catalog))

    private suspend fun ids(
        query: ProductQuery = ProductQuery(),
        page: PageRequest = PageRequest(),
    ) = listProducts(query, page).items.map { it.id }

    @Test
    fun `without filters every product is returned, ordered by id`() =
        runTest {
            ids() shouldBe listOf(1L, 2, 3, 4, 5)
        }

    @Test
    fun `brand is an exact match that ignores case`() =
        runTest {
            ids(ProductQuery(brand = "nike")) shouldBe listOf(3L, 4)
            ids(ProductQuery(brand = "Nik")) shouldBe emptyList()
        }

    @Test
    fun `category filters to that category only`() =
        runTest {
            ids(ProductQuery(category = "Electronics")) shouldBe listOf(1L, 2, 5)
        }

    @Test
    fun `the price range is inclusive at both ends`() =
        runTest {
            ids(ProductQuery(priceMin = BigDecimal("129.99"), priceMax = BigDecimal("399.99"))) shouldBe listOf(1L, 2, 3)
        }

    @Test
    fun `filters combine with AND`() =
        runTest {
            ids(ProductQuery(brand = "Apple", category = "Electronics", priceMax = BigDecimal("500"))) shouldBe listOf(1L)
        }

    @Test
    fun `results are list models, reduced from the full details after filtering`() =
        runTest {
            val items = listProducts(ProductQuery(brand = "Sony")).items

            items shouldBe listOf(catalog.first { it.id == 2L }.toProduct())
        }

    @Test
    fun `a blank filter is treated as no filter`() =
        runTest {
            ids(ProductQuery(brand = " ")) shouldBe listOf(1L, 2, 3, 4, 5)
        }

    @Test
    fun `pages are cut after filtering, and carry the filtered totals`() =
        runTest {
            val page = listProducts(ProductQuery(category = "Electronics"), PageRequest(page = 2, size = 2))

            page.items.map { it.id } shouldBe listOf(5L)
            page.totalItems shouldBe 3
            page.totalPages shouldBe 2
        }

    @Test
    fun `a minimum price above the maximum is rejected with an actionable message`() {
        val error = shouldThrow<InvalidInputException> { ProductQuery(priceMin = BigDecimal("50"), priceMax = BigDecimal("10")) }

        error.code shouldBe "INVALID_PRICE_RANGE"
        error.message shouldBe "price_min (50) must not be greater than price_max (10)."
    }

    @Test
    fun `a negative price bound is rejected`() {
        shouldThrow<InvalidInputException> { ProductQuery(priceMax = BigDecimal("-1")) }.field shouldBe "price_max"
    }
}
