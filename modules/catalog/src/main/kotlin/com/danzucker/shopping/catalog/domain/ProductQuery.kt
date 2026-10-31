package com.danzucker.shopping.catalog.domain

import com.danzucker.shopping.common.core.InvalidInputException
import java.math.BigDecimal

/**
 * Every way a client can narrow the catalog, in one place, so `GET /products` and
 * `GET /products/search` share exactly the same filtering rules.
 *
 * Matching rules:
 * - [text]: case-insensitive substring of the name OR the description ("partial matches").
 * - [brand] and [category]: exact match, ignoring case, so `nike` finds `Nike`.
 * - [priceMin] and [priceMax]: inclusive bounds.
 *
 * Blank strings mean "no filter", so `?brand=` behaves like leaving `brand` out.
 */
internal data class ProductQuery(
    val text: String? = null,
    val brand: String? = null,
    val category: String? = null,
    val priceMin: BigDecimal? = null,
    val priceMax: BigDecimal? = null,
) {
    init {
        priceMin?.let { if (it.signum() < 0) throw negativePrice("price_min", it) }
        priceMax?.let { if (it.signum() < 0) throw negativePrice("price_max", it) }
        if (priceMin != null && priceMax != null && priceMin > priceMax) {
            throw InvalidInputException(
                "INVALID_PRICE_RANGE",
                "price_min (${priceMin.toPlainString()}) must not be greater than price_max (${priceMax.toPlainString()}).",
                "price_min",
            )
        }
    }

    fun matches(product: Product): Boolean =
        matchesText(product) &&
            (brand.isNullOrBlank() || product.brand.equals(brand.trim(), ignoreCase = true)) &&
            (category.isNullOrBlank() || product.category.equals(category.trim(), ignoreCase = true)) &&
            (priceMin == null || product.price >= priceMin) &&
            (priceMax == null || product.price <= priceMax)

    private fun matchesText(product: Product): Boolean {
        val term = text?.trim().orEmpty()
        return term.isEmpty() || product.name.contains(term, ignoreCase = true) || product.description.contains(term, ignoreCase = true)
    }

    private fun negativePrice(
        field: String,
        value: BigDecimal,
    ) = InvalidInputException("INVALID_PRICE", "$field must be 0 or greater; got ${value.toPlainString()}.", field)
}
