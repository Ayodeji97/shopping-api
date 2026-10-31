package com.danzucker.shopping.catalog

import com.danzucker.shopping.catalog.domain.Product
import java.math.BigDecimal

/** Builds a product with sensible defaults, so each test states only what it is about. */
@Suppress("LongParameterList") // A test builder: every parameter has a default.
internal fun product(
    id: Long,
    name: String = "Product $id",
    description: String = "Description of product $id",
    price: String = "10.00",
    brand: String = "Acme",
    category: String = "Misc",
) = Product(id, name, description, "https://example.com/$id.jpg", BigDecimal(price), brand, category)
