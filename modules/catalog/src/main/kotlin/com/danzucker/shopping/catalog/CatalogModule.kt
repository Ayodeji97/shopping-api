package com.danzucker.shopping.catalog

import com.danzucker.shopping.catalog.api.ProductCatalog
import com.danzucker.shopping.catalog.data.InMemoryProductRepository
import com.danzucker.shopping.catalog.data.SeedProducts
import com.danzucker.shopping.catalog.domain.ProductRepository
import com.danzucker.shopping.catalog.usecase.GetProduct
import com.danzucker.shopping.catalog.usecase.ListProducts
import com.danzucker.shopping.catalog.usecase.ProductCatalogAdapter
import com.danzucker.shopping.catalog.usecase.SearchProducts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Dependency-injection wiring for the catalog module: its composition root.
 *
 * The `domain` and `usecase` layers carry no Spring annotations (an architecture test enforces
 * this), so the business rules are plain Kotlin that can be tested and reused without a framework.
 * This file is the one place that knows which implementation backs each abstraction.
 */
@Configuration(proxyBeanMethods = false)
internal class CatalogModule {
    /** Data source, seeded at startup with the catalog's products. */
    @Bean
    fun productRepository(): ProductRepository = InMemoryProductRepository(SeedProducts.all)

    @Bean
    fun listProducts(products: ProductRepository) = ListProducts(products)

    @Bean
    fun searchProducts(listProducts: ListProducts) = SearchProducts(listProducts)

    @Bean
    fun getProduct(products: ProductRepository) = GetProduct(products)

    /** The module's public API, the only catalog bean other modules may depend on. */
    @Bean
    fun productCatalog(products: ProductRepository): ProductCatalog = ProductCatalogAdapter(products)
}
