package com.danzucker.shopping.catalog.api

/**
 * The catalog module's public API: the only part of the module other modules may use.
 *
 * Everything else in `catalog` is Kotlin `internal`, so the wishlist module cannot reach into the
 * catalog's repository or domain model. It can only ask this narrow question. If the catalog later
 * moves to a database or a separate service, only the implementation of this interface changes.
 */
interface ProductCatalog {
    /** Whether a product with [productId] exists in the catalog. */
    suspend fun exists(productId: Long): Boolean
}
