package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.domain.ProductDetails
import com.danzucker.shopping.catalog.domain.ProductNotFoundException
import com.danzucker.shopping.catalog.domain.ProductRepository

/** Use case: fetch one product's full [ProductDetails], failing with [ProductNotFoundException] if it does not exist. */
internal class GetProductDetails(
    private val products: ProductRepository,
) {
    suspend operator fun invoke(id: Long): ProductDetails = products.findById(id) ?: throw ProductNotFoundException(id)
}
