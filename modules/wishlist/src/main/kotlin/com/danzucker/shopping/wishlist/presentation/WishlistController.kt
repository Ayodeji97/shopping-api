package com.danzucker.shopping.wishlist.presentation

import com.danzucker.shopping.wishlist.domain.SessionId
import com.danzucker.shopping.wishlist.usecase.AddToWishlist
import com.danzucker.shopping.wishlist.usecase.GetWishlist
import com.danzucker.shopping.wishlist.usecase.RemoveFromWishlist
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Wishlist endpoints. Every call must send an `X-Session-Id` header naming whose wishlist it is
 * (see `InMemoryWishlistRepository` for why wishlists are scoped per session). A missing or
 * malformed header is a 400 with instructions.
 *
 * The header is read as an optional string and validated by [SessionId.parse] rather than
 * declared `required`, so the error message is ours and actionable, not Spring's generic one.
 */
@RestController
@RequestMapping("/wishlist")
internal class WishlistController(
    private val getWishlist: GetWishlist,
    private val addToWishlist: AddToWishlist,
    private val removeFromWishlist: RemoveFromWishlist,
) {
    /**
     * `GET /wishlist`: the product ids in this session's wishlist, oldest first.
     *
     * 200 `{"productIds": [...]}` (an empty list for a new session); 400 without a valid `X-Session-Id`.
     */
    @GetMapping
    suspend fun getWishlist(
        @RequestHeader(SessionId.HEADER, required = false) sessionId: String?,
    ): WishlistResponse = WishlistResponse(getWishlist.invoke(SessionId.parse(sessionId)))

    /**
     * `POST /wishlist/{productId}`: add a product to this session's wishlist.
     *
     * 201 with the updated wishlist; 404 if the product does not exist; 409 if it is already in
     * the wishlist; 400 without a valid `X-Session-Id` or for a non-numeric id.
     */
    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun addToWishlist(
        @RequestHeader(SessionId.HEADER, required = false) sessionId: String?,
        @PathVariable productId: Long,
    ): WishlistResponse = WishlistResponse(addToWishlist.invoke(SessionId.parse(sessionId), productId))

    /**
     * `DELETE /wishlist/{productId}`: remove a product from this session's wishlist.
     *
     * 204 with no body; 404 if the product is not in the wishlist; 400 without a valid `X-Session-Id`.
     */
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun removeFromWishlist(
        @RequestHeader(SessionId.HEADER, required = false) sessionId: String?,
        @PathVariable productId: Long,
    ) {
        removeFromWishlist.invoke(SessionId.parse(sessionId), productId)
    }
}

/** The wishlist as the API returns it: product ids only, as the spec defines. */
internal data class WishlistResponse(
    val productIds: List<Long>,
)
