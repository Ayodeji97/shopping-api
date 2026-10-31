package com.danzucker.shopping.catalog.domain

import com.danzucker.shopping.common.core.DomainException

internal class ProductNotFoundException(
    id: Long,
) : DomainException(
        Kind.NOT_FOUND,
        "PRODUCT_NOT_FOUND",
        "No product with id $id exists. List available products with GET /products.",
    )
