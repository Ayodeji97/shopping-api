package com.danzucker.shopping.catalog.presentation

import com.danzucker.shopping.catalog.domain.Product
import com.danzucker.shopping.catalog.domain.ProductDetails
import java.math.BigDecimal

/*
 * The JSON contracts for the catalog's two domain models:
 * - [ProductResponse] maps the list model `Product`. It is used by `GET /products` and
 *   `GET /products/search`, and has no brand or category.
 * - [ProductDetailsResponse] maps `ProductDetails`. It is used by `GET /products/{id}`, and has
 *   every field.
 *
 * They are kept separate from the domain models so the API contract can change independently.
 */

/** One item in a product list or search result: what a product card displays. */
internal data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: BigDecimal,
) {
    companion object {
        fun from(product: Product) =
            ProductResponse(
                id = product.id,
                name = product.name,
                description = product.description,
                imageUrl = product.imageUrl,
                price = product.price,
            )
    }
}

/** A product's full details, as returned by `GET /products/{id}`. */
internal data class ProductDetailsResponse(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: BigDecimal,
    val brand: String,
    val category: String,
) {
    companion object {
        fun from(product: ProductDetails) =
            ProductDetailsResponse(
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
