package com.danzucker.shopping.wishlist

import com.danzucker.shopping.catalog.api.ProductCatalog

/** A catalog that knows exactly the given ids. Deliberately a fake, not a mock: it behaves like the real thing. */
internal class FakeProductCatalog(
    private val knownIds: Set<Long>,
) : ProductCatalog {
    override suspend fun exists(productId: Long): Boolean = productId in knownIds
}
