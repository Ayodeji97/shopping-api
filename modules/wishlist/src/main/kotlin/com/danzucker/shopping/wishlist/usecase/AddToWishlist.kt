package com.danzucker.shopping.wishlist.usecase

import com.danzucker.shopping.catalog.api.ProductCatalog
import com.danzucker.shopping.wishlist.domain.AlreadyInWishlistException
import com.danzucker.shopping.wishlist.domain.SessionId
import com.danzucker.shopping.wishlist.domain.UnknownProductException
import com.danzucker.shopping.wishlist.domain.WishlistRepository
import kotlinx.coroutines.flow.first

/**
 * Use case: add a product to a session's wishlist.
 *
 * Rules:
 * 1. The product must exist in the catalog, otherwise [UnknownProductException] (404). The catalog
 *    is asked through its public [ProductCatalog] API only.
 * 2. A product can be in a wishlist once, otherwise [AlreadyInWishlistException] (409). A conflict
 *    tells a client its local state is stale; silently succeeding would hide that.
 *
 * Returns the wishlist after the change.
 */
internal class AddToWishlist(
    private val catalog: ProductCatalog,
    private val wishlists: WishlistRepository,
) {
    suspend operator fun invoke(
        session: SessionId,
        productId: Long,
    ): List<Long> {
        if (!catalog.exists(productId)) throw UnknownProductException(productId)
        if (!wishlists.add(session, productId)) throw AlreadyInWishlistException(productId)
        return wishlists.observe(session).first()
    }
}
