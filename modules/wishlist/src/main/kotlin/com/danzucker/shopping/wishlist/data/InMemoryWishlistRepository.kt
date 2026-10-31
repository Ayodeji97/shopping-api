package com.danzucker.shopping.wishlist.data

import com.danzucker.shopping.wishlist.domain.SessionId
import com.danzucker.shopping.wishlist.domain.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory wishlists, **one per session**: the map key is the client's `X-Session-Id`.
 *
 * ## Why per-session and not one singleton list
 * The spec allows either. A single list shared by every user would mean one shopper's wishlist
 * shows up on everyone else's phone, which no real shopping app does. Scoping by session behaves
 * like a real per-user wishlist without needing authentication.
 *
 * ## Why a header and not a cookie (`HttpSession`)
 * The client is a mobile app. Native HTTP clients do not keep cookies by default, while sending a
 * header is one line. A client-generated id (a UUID stored on the device) also survives server
 * restarts on the client side, and maps cleanly to a user id once authentication exists: only this
 * key would change.
 *
 * ## Concurrency
 * Each session's list is a [MutableStateFlow] changed with `update` (an atomic compare-and-set loop), so concurrent adds or
 * removes on one session never lose an update, and different sessions never contend.
 *
 * ## Limits (acceptable for this assessment)
 * Data lives only as long as the process, and sessions are never evicted. A production version
 * would put this behind the same [WishlistRepository] interface on Redis or a database, with a TTL.
 * Reads never create a session, so only clients that actually add something use memory.
 */
internal class InMemoryWishlistRepository : WishlistRepository {
    private val sessions = ConcurrentHashMap<SessionId, MutableStateFlow<List<Long>>>()

    override fun observe(session: SessionId): Flow<List<Long>> = sessions[session]?.asStateFlow() ?: flowOf(emptyList())

    override suspend fun add(
        session: SessionId,
        productId: Long,
    ): Boolean {
        val state = sessions.computeIfAbsent(session) { MutableStateFlow(emptyList()) }
        var added = false
        // `update` retries the lambda until its compare-and-set wins, so `added` reflects the attempt that took effect.
        state.update { current ->
            added = productId !in current
            if (added) current + productId else current
        }
        return added
    }

    override suspend fun remove(
        session: SessionId,
        productId: Long,
    ): Boolean {
        val state = sessions[session] ?: return false
        var removed = false
        state.update { current ->
            removed = productId in current
            current - productId
        }
        return removed
    }
}
