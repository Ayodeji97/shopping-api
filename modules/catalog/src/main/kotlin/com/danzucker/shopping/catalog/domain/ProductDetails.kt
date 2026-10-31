package com.danzucker.shopping.catalog.domain

import java.math.BigDecimal

/**
 * A product's full details: everything the catalog knows about it, including [brand] and
 * [category]. This is the catalog's complete record, and what the data source stores.
 *
 * Lists are built from it with [toProduct], so the list model can never contain anything the
 * details don't.
 */
internal data class ProductDetails(
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

    /** The list model of this product: what a product card shows. */
    fun toProduct(): Product = Product(id = id, name = name, description = description, imageUrl = imageUrl, price = price)
}
