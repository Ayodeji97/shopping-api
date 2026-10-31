package com.danzucker.shopping.catalog.data

import com.danzucker.shopping.catalog.domain.Product
import com.danzucker.shopping.catalog.domain.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

/**
 * The catalog's data source: an immutable, in-memory list of products.
 *
 * The catalog is read-only in this API, so an immutable map is safe to share across concurrent
 * requests without locking. Swapping in a database means writing another [ProductRepository];
 * no use case changes.
 */
internal class InMemoryProductRepository(
    products: List<Product>,
) : ProductRepository {
    private val byId: Map<Long, Product> = products.associateBy(Product::id)

    init {
        require(byId.size == products.size) { "Product ids must be unique." }
    }

    override fun observeAll(): Flow<Product> = byId.values.asFlow()

    override suspend fun findById(id: Long): Product? = byId[id]
}
