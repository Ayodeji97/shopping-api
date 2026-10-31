package com.danzucker.shopping.catalog.domain

import java.math.BigDecimal

/**
 * A product as it appears in a list or search result: what a product card displays.
 *
 * The catalog has two models of a product because the screens need two different amounts of it.
 * A product grid shows the name, image, price and a short description; brand and category only
 * appear on the details screen ([ProductDetails]). Keeping the list model small also keeps each
 * page light on mobile networks.
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
)
