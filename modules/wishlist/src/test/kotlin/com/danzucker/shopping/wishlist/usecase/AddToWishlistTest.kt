package com.danzucker.shopping.wishlist.usecase

import com.danzucker.shopping.wishlist.FakeProductCatalog
import com.danzucker.shopping.wishlist.data.InMemoryWishlistRepository
import com.danzucker.shopping.wishlist.domain.AlreadyInWishlistException
import com.danzucker.shopping.wishlist.domain.SessionId
import com.danzucker.shopping.wishlist.domain.UnknownProductException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

internal class AddToWishlistTest {
    private val wishlists = InMemoryWishlistRepository()
    private val addToWishlist = AddToWishlist(FakeProductCatalog(knownIds = setOf(1, 2, 3)), wishlists)
    private val getWishlist = GetWishlist(wishlists)
    private val alice = SessionId.parse("alice")
    private val bob = SessionId.parse("bob")

    @Test
    fun `adding returns the wishlist in the order products were added`() =
        runTest {
            addToWishlist(alice, 3)

            addToWishlist(alice, 1) shouldBe listOf(3L, 1)
            getWishlist(alice) shouldBe listOf(3L, 1)
        }

    @Test
    fun `adding a product twice is a conflict and leaves the wishlist unchanged`() =
        runTest {
            addToWishlist(alice, 2)

            shouldThrow<AlreadyInWishlistException> { addToWishlist(alice, 2) }.code shouldBe "ALREADY_IN_WISHLIST"
            getWishlist(alice) shouldBe listOf(2L)
        }

    @Test
    fun `a product missing from the catalog cannot be added`() =
        runTest {
            shouldThrow<UnknownProductException> { addToWishlist(alice, 99) }.code shouldBe "PRODUCT_NOT_FOUND"
            getWishlist(alice) shouldBe emptyList()
        }

    @Test
    fun `each session has its own wishlist`() =
        runTest {
            addToWishlist(alice, 1)
            addToWishlist(bob, 2)

            getWishlist(alice) shouldBe listOf(1L)
            getWishlist(bob) shouldBe listOf(2L)
        }

    @Test
    fun `the same product can be in two sessions' wishlists`() =
        runTest {
            addToWishlist(alice, 1)

            addToWishlist(bob, 1) shouldBe listOf(1L)
        }
}
