package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.data.InMemoryProductRepository
import com.danzucker.shopping.catalog.domain.ProductQuery
import com.danzucker.shopping.catalog.product
import com.danzucker.shopping.common.core.InvalidInputException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class SearchProductsTest {
    private val catalog =
        listOf(
            product(1, name = "Wireless Earbuds", description = "Small and light", category = "Electronics"),
            product(2, name = "Headphones", description = "Over-ear, WIRELESS, noise cancelling", category = "Electronics"),
            product(3, name = "Running Shoes", description = "Light trainers", category = "Footwear"),
        )
    private val search = SearchProducts(ListProducts(InMemoryProductRepository(catalog)))

    private suspend fun ids(query: ProductQuery) = search(query).items.map { it.id }

    @Test
    fun `matches the name or the description, partially and ignoring case`() =
        runTest {
            ids(ProductQuery(text = "wireless")) shouldBe listOf(1L, 2)
            ids(ProductQuery(text = "LIGHT")) shouldBe listOf(1L, 3)
        }

    @Test
    fun `surrounding whitespace in the term is ignored`() =
        runTest {
            ids(ProductQuery(text = "  shoes ")) shouldBe listOf(3L)
        }

    @Test
    fun `search results can be narrowed by the catalog filters`() =
        runTest {
            ids(ProductQuery(text = "light", category = "Footwear")) shouldBe listOf(3L)
        }

    @Test
    fun `no match is an empty page, not an error`() =
        runTest {
            search(ProductQuery(text = "toaster")).totalItems shouldBe 0
        }

    @Test
    fun `a blank term is rejected and says how to search`() =
        runTest {
            val error = shouldThrow<InvalidInputException> { search(ProductQuery(text = "   ")) }

            error.code shouldBe "MISSING_SEARCH_QUERY"
            error.field shouldBe "query"
        }
}
