package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.api.ProductCatalog
import com.danzucker.shopping.catalog.domain.ProductRepository

/** Implements the module's public [ProductCatalog] API on top of the internal repository. */
internal class ProductCatalogAdapter(
    private val products: ProductRepository,
) : ProductCatalog {
    override suspend fun exists(productId: Long): Boolean = products.findById(productId) != null
}
