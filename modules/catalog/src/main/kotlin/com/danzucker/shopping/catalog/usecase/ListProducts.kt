package com.danzucker.shopping.catalog.usecase

import com.danzucker.shopping.catalog.domain.Product
import com.danzucker.shopping.catalog.domain.ProductDetails
import com.danzucker.shopping.catalog.domain.ProductQuery
import com.danzucker.shopping.catalog.domain.ProductRepository
import com.danzucker.shopping.common.core.Page
import com.danzucker.shopping.common.core.PageRequest
import com.danzucker.shopping.common.core.paginate
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

/**
 * Use case: list the catalog, optionally filtered, one page at a time.
 *
 * The repository streams full [ProductDetails]. Each one is filtered against the query (so brand
 * and category filters work) and then reduced to the list model [Product], all inside the
 * [kotlinx.coroutines.flow.Flow], so only matching products are ever collected.
 *
 * Results are ordered by id, so pages are stable: the same request always returns the same page,
 * and paging through never skips or repeats a product.
 */
internal class ListProducts(
    private val products: ProductRepository,
) {
    suspend operator fun invoke(
        query: ProductQuery = ProductQuery(),
        page: PageRequest = PageRequest(),
    ): Page<Product> =
        products
            .observeAll()
            .filter(query::matches)
            .map(ProductDetails::toProduct)
            .toList()
            .sortedBy(Product::id)
            .paginate(page)
}
