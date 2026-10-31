package com.danzucker.shopping.catalog.domain

import java.math.BigDecimal

/**
 * A product in the catalog: the core entity of this module.
 *
 * [price] is a [BigDecimal] rather than a `Double`, so prices such as 19.99 are exact and range
 * filters never miss a product because of floating-point rounding.
 */
internal data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: BigDecimal,
    val brand: String,
    val category: String,
) {
    init {
        require(name.isNotBlank()) { "A product needs a name." }
        require(price.signum() >= 0) { "A product's price cannot be negative." }
    }
}
