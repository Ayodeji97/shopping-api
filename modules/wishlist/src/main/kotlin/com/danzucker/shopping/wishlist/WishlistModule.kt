package com.danzucker.shopping.wishlist

import com.danzucker.shopping.catalog.api.ProductCatalog
import com.danzucker.shopping.wishlist.data.InMemoryWishlistRepository
import com.danzucker.shopping.wishlist.domain.WishlistRepository
import com.danzucker.shopping.wishlist.usecase.AddToWishlist
import com.danzucker.shopping.wishlist.usecase.GetWishlist
import com.danzucker.shopping.wishlist.usecase.RemoveFromWishlist
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Dependency-injection wiring for the wishlist module: its composition root.
 *
 * [ProductCatalog] arrives from the catalog module's public API. That is the only cross-module
 * dependency in the application.
 */
@Configuration(proxyBeanMethods = false)
internal class WishlistModule {
    /** Singleton in-memory store holding one wishlist per session; see [InMemoryWishlistRepository]. */
    @Bean
    fun wishlistRepository(): WishlistRepository = InMemoryWishlistRepository()

    @Bean
    fun getWishlist(wishlists: WishlistRepository) = GetWishlist(wishlists)

    @Bean
    fun addToWishlist(
        catalog: ProductCatalog,
        wishlists: WishlistRepository,
    ) = AddToWishlist(catalog, wishlists)

    @Bean
    fun removeFromWishlist(wishlists: WishlistRepository) = RemoveFromWishlist(wishlists)
}
