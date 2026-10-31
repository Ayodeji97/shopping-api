package com.danzucker.shopping.wishlist.usecase

import com.danzucker.shopping.wishlist.domain.NotInWishlistException
import com.danzucker.shopping.wishlist.domain.SessionId
import com.danzucker.shopping.wishlist.domain.WishlistRepository

/**
 * Use case: remove a product from a session's wishlist.
 *
 * Removing a product that is not there fails with [NotInWishlistException] (404), so a client
 * learns its local copy of the wishlist is out of date. The catalog is not consulted: removing an
 * id is valid even if the product has since left the catalog.
 */
internal class RemoveFromWishlist(
    private val wishlists: WishlistRepository,
) {
    suspend operator fun invoke(
        session: SessionId,
        productId: Long,
    ) {
        if (!wishlists.remove(session, productId)) throw NotInWishlistException(productId)
    }
}
