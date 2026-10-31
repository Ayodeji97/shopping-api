package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.domain.Product
import com.danzucker.shopping.catalog.domain.ProductQuery
import com.danzucker.shopping.common.core.InvalidInputException
import com.danzucker.shopping.common.core.Page
import com.danzucker.shopping.common.core.PageRequest

/**
 * Use case: find products whose name or description contains a search term (case-insensitive,
 * partial match). The brand, category and price filters can narrow a search further.
 *
 * A search without a term is rejected rather than treated as "everything": a client that sends an
 * empty search box almost certainly has a bug, and silently listing the whole catalog would hide it.
 */
internal class SearchProducts(
    private val listProducts: ListProducts,
) {
    suspend operator fun invoke(
        query: ProductQuery,
        page: PageRequest = PageRequest(),
    ): Page<Product> {
        if (query.text.isNullOrBlank()) {
            throw InvalidInputException(
                "MISSING_SEARCH_QUERY",
                "Add a search term, e.g. /products/search?query=headphones.",
                "query",
            )
        }
        return listProducts(query, page)
    }
}
