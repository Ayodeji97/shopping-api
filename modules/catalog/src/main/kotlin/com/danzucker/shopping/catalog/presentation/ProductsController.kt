package com.danzucker.shopping.catalog.presentation

import com.danzucker.shopping.catalog.domain.ProductQuery
import com.danzucker.shopping.catalog.usecase.GetProductDetails
import com.danzucker.shopping.catalog.usecase.ListProducts
import com.danzucker.shopping.catalog.usecase.SearchProducts
import com.danzucker.shopping.common.core.PageRequest
import com.danzucker.shopping.common.web.PageResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

/**
 * Product catalog endpoints. The controller only translates HTTP to use-case calls: parsing,
 * validation rules and filtering live in the domain and use cases.
 *
 * Handlers are `suspend` functions; Spring MVC runs them as coroutines.
 */
@RestController
@RequestMapping("/products")
internal class ProductsController(
    private val listProducts: ListProducts,
    private val searchProducts: SearchProducts,
    private val getProductDetails: GetProductDetails,
) {
    /**
     * `GET /products`: list products, 20 per page by default, ordered by id.
     *
     * Optional filters, combined with AND:
     * - `brand`, `category`: exact match, ignoring case.
     * - `price_min`, `price_max`: inclusive price range.
     *
     * Pagination: `page` (1-based, default 1) and `size` (1-100, default 20).
     *
     * 200 with a page envelope of `ProductResponse` items (no brand or category);
     * 400 for a bad page, size, price or price range.
     */
    @GetMapping
    suspend fun listProducts(
        @RequestParam(required = false) brand: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam("price_min", required = false) priceMin: BigDecimal?,
        @RequestParam("price_max", required = false) priceMax: BigDecimal?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "${PageRequest.DEFAULT_SIZE}") size: Int,
    ): PageResponse<ProductResponse> {
        val query = ProductQuery(brand = brand, category = category, priceMin = priceMin, priceMax = priceMax)
        return PageResponse.from(listProducts(query, PageRequest(page, size)), ProductResponse::from)
    }

    /**
     * `GET /products/search?query=term`: products whose name or description contains `query`
     * (case-insensitive, partial match).
     *
     * Accepts the same filters and pagination as `GET /products`.
     *
     * 200 with a page envelope of `ProductResponse` items; 400 if `query` is missing or blank, or for any bad filter.
     */
    @GetMapping("/search")
    suspend fun searchProducts(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) brand: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam("price_min", required = false) priceMin: BigDecimal?,
        @RequestParam("price_max", required = false) priceMax: BigDecimal?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "${PageRequest.DEFAULT_SIZE}") size: Int,
    ): PageResponse<ProductResponse> {
        val productQuery = ProductQuery(text = query, brand = brand, category = category, priceMin = priceMin, priceMax = priceMax)
        return PageResponse.from(searchProducts(productQuery, PageRequest(page, size)), ProductResponse::from)
    }

    /**
     * `GET /products/{id}`: one product's full details, including brand and category.
     *
     * 200 with the product; 404 if no product has this id; 400 if `id` is not a whole number.
     */
    @GetMapping("/{id}")
    suspend fun getProductDetails(
        @PathVariable id: Long,
    ): ProductDetailsResponse = ProductDetailsResponse.from(getProductDetails.invoke(id))
}
