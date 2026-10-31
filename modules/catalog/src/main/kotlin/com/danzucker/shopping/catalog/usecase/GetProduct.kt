package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.domain.Product
import com.danzucker.shopping.catalog.domain.ProductNotFoundException
import com.danzucker.shopping.catalog.domain.ProductRepository

/** Use case: fetch one product's details, failing with [ProductNotFoundException] if it does not exist. */
internal class GetProduct(
    private val products: ProductRepository,
) {
    suspend operator fun invoke(id: Long): Product = products.findById(id) ?: throw ProductNotFoundException(id)
}
