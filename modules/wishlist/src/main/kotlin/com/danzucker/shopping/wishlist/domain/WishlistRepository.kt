package com.danzucker.shopping.wishlist.domain

import kotlinx.coroutines.flow.Flow

/**
 * Storage for wishlists, one per [SessionId]. Declared in the domain, implemented in `infra`.
 *
 * Add and remove are single atomic operations that report whether anything changed, rather than a
 * read-modify-write the use case performs. Two concurrent requests from the same session can then
 * never lose an update or both "succeed" at adding the same product.
 */
internal interface WishlistRepository {
    /** The session's product ids in the order they were added. Emits the current list, then every change. */
    fun observe(session: SessionId): Flow<List<Long>>

    /** Adds [productId]; returns `false` (and changes nothing) if it was already there. */
    suspend fun add(
        session: SessionId,
        productId: Long,
    ): Boolean

    /** Removes [productId]; returns `false` if it was not there. */
    suspend fun remove(
        session: SessionId,
        productId: Long,
    ): Boolean
}
