package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.data.InMemoryProductRepository
import com.danzucker.shopping.catalog.domain.ProductNotFoundException
import com.danzucker.shopping.catalog.product
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class GetProductDetailsTest {
    private val getProductDetails =
        GetProductDetails(InMemoryProductRepository(listOf(product(7, name = "Lamp", brand = "IKEA", category = "Home"))))

    @Test
    fun `returns the full details, including brand and category`() =
        runTest {
            val details = getProductDetails(7)

            details.name shouldBe "Lamp"
            details.brand shouldBe "IKEA"
            details.category shouldBe "Home"
        }

    @Test
    fun `an unknown id is a not-found failure that points to the product list`() =
        runTest {
            val error = shouldThrow<ProductNotFoundException> { getProductDetails(99) }

            error.code shouldBe "PRODUCT_NOT_FOUND"
            error.message shouldBe "No product with id 99 exists. List available products with GET /products."
        }
}
