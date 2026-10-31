package com.danzucker.shopping.catalog.domain

import com.danzucker.shopping.catalog.product
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class ProductDetailsTest {
    @Test
    fun `the list model keeps what a product card shows and nothing else`() {
        val details = product(4, name = "Lamp", description = "Warm light", price = "19.99", brand = "IKEA", category = "Home")

        details.toProduct() shouldBe
            Product(id = 4, name = "Lamp", description = "Warm light", imageUrl = details.imageUrl, price = BigDecimal("19.99"))
    }

    @Test
    fun `a product cannot have a negative price`() {
        shouldThrow<IllegalArgumentException> { product(1, price = "-0.01") }
    }
}
