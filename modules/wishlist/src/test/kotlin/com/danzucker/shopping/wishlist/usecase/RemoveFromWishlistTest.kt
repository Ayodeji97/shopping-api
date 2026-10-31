package com.danzucker.shopping.wishlist.usecase

import com.danzucker.shopping.wishlist.FakeProductCatalog
import com.danzucker.shopping.wishlist.data.InMemoryWishlistRepository
import com.danzucker.shopping.wishlist.domain.NotInWishlistException
import com.danzucker.shopping.wishlist.domain.SessionId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class RemoveFromWishlistTest {
    private val wishlists = InMemoryWishlistRepository()
    private val addToWishlist = AddToWishlist(FakeProductCatalog(knownIds = setOf(1, 2, 3)), wishlists)
    private val removeFromWishlist = RemoveFromWishlist(wishlists)
    private val getWishlist = GetWishlist(wishlists)
    private val alice = SessionId.parse("alice")

    @Test
    fun `removing keeps the order of what is left`() =
        runTest {
            addToWishlist(alice, 1)
            addToWishlist(alice, 2)
            addToWishlist(alice, 3)

            removeFromWishlist(alice, 2)

            getWishlist(alice) shouldBe listOf(1L, 3)
        }

    @Test
    fun `removing a product that is not in the wishlist is a not-found failure`() =
        runTest {
            addToWishlist(alice, 1)

            shouldThrow<NotInWishlistException> { removeFromWishlist(alice, 2) }.code shouldBe "NOT_IN_WISHLIST"
        }

    @Test
    fun `removing twice fails the second time`() =
        runTest {
            addToWishlist(alice, 1)
            removeFromWishlist(alice, 1)

            shouldThrow<NotInWishlistException> { removeFromWishlist(alice, 1) }
        }

    @Test
    fun `removing from a session that never added anything is a not-found failure`() =
        runTest {
            shouldThrow<NotInWishlistException> { removeFromWishlist(SessionId.parse("new-session"), 1) }
        }

    @Test
    fun `a removed product can be added again`() =
        runTest {
            addToWishlist(alice, 1)
            removeFromWishlist(alice, 1)

            addToWishlist(alice, 1) shouldBe listOf(1L)
        }
}
