package com.danzucker.shopping.catalog.domain

import kotlinx.coroutines.flow.Flow

/**
 * Where products come from. Declared in the domain and implemented in `infra`, so use cases depend
 * on this abstraction and not on any storage technology (the dependency-inversion rule).
 */
internal interface ProductRepository {
    /** Every product in the catalog, as a cold [Flow]: a data source can stream instead of loading everything at once. */
    fun observeAll(): Flow<Product>

    /** The product with [id], or `null` if there is none. */
    suspend fun findById(id: Long): Product?
}
