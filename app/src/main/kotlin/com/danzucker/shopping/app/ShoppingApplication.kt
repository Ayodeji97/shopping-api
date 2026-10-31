package com.danzucker.shopping.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Shopping Wishlist & Product Discovery API: a modular monolith.
 *
 * - `modules/catalog`: products, search, filtering (read-only, seeded at startup).
 * - `modules/wishlist`: per-session in-memory wishlists; depends only on `catalog.api`.
 * - `common/core`, `common/web`: shared pagination, domain error types and the HTTP error mapping.
 *
 * Component scanning starts at `com.danzucker.shopping`, so each module's own `@Configuration`
 * (its composition root) and controllers are picked up from here.
 */
@SpringBootApplication(scanBasePackages = ["com.danzucker.shopping"])
class ShoppingApplication

@Suppress("SpreadOperator") // The idiomatic Spring Boot entry point; runs once at startup.
fun main(args: Array<String>) {
    runApplication<ShoppingApplication>(*args)
}
