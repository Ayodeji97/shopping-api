package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.data.InMemoryProductRepository
import com.danzucker.shopping.catalog.domain.ProductNotFoundException
import com.danzucker.shopping.catalog.product
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class GetProductTest {
    private val getProduct = GetProduct(InMemoryProductRepository(listOf(product(7, name = "Lamp"))))

    @Test
    fun `returns the product with that id`() =
        runTest {
            getProduct(7).name shouldBe "Lamp"
        }

    @Test
    fun `an unknown id is a not-found failure that points to the product list`() =
        runTest {
            val error = shouldThrow<ProductNotFoundException> { getProduct(99) }

            error.code shouldBe "PRODUCT_NOT_FOUND"
            error.message shouldBe "No product with id 99 exists. List available products with GET /products."
        }
}
