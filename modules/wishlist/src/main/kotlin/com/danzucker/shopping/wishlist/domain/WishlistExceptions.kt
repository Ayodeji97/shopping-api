package com.danzucker.shopping.wishlist.domain

import com.danzucker.shopping.common.core.DomainException

internal class AlreadyInWishlistException(
    productId: Long,
) : DomainException(
        Kind.CONFLICT,
        "ALREADY_IN_WISHLIST",
        "Product $productId is already in your wishlist. Fetch it with GET /wishlist.",
    )

internal class NotInWishlistException(
    productId: Long,
) : DomainException(
        Kind.NOT_FOUND,
        "NOT_IN_WISHLIST",
        "Product $productId is not in your wishlist, so there is nothing to remove. Fetch it with GET /wishlist.",
    )

internal class UnknownProductException(
    productId: Long,
) : DomainException(
        Kind.NOT_FOUND,
        "PRODUCT_NOT_FOUND",
        "No product with id $productId exists, so it cannot be added. List available products with GET /products.",
    )
