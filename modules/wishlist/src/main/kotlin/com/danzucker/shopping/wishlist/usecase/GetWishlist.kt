package com.danzucker.shopping.wishlist.usecase

import com.danzucker.shopping.wishlist.domain.SessionId
import com.danzucker.shopping.wishlist.domain.WishlistRepository
import kotlinx.coroutines.flow.first

/** Use case: the product ids in a session's wishlist, oldest first. A session that has never added anything has an empty wishlist. */
internal class GetWishlist(
    private val wishlists: WishlistRepository,
) {
    suspend operator fun invoke(session: SessionId): List<Long> = wishlists.observe(session).first()
}
