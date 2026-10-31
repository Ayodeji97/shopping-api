package com.danzucker.shopping.catalog.presentation

import com.danzucker.shopping.catalog.domain.Product
import java.math.BigDecimal

/** A product as the API returns it. Kept separate from the domain [Product] so the JSON contract can change independently of it. */
internal data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: BigDecimal,
    val brand: String,
    val category: String,
) {
    companion object {
        fun from(product: Product) =
            ProductResponse(
                id = product.id,
                name = product.name,
                description = product.description,
                imageUrl = product.imageUrl,
                price = product.price,
                brand = product.brand,
                category = product.category,
            )
    }
}
