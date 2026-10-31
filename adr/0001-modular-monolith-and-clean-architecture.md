# 0001. Modular monolith with Clean Architecture layers

**Status:** Accepted, 2026-10-31

## Context
The project is built around Clean Architecture (entities, repositories, data sources, use cases, a controller layer), dependency injection, and Coroutines/Flow. The API has two features, a catalog and a wishlist, and the wishlist needs to know whether a product exists.

## Decision
- **One deployable, several Gradle modules:** `app` (composition root), `common:core`, `common:web`, `modules:catalog` and `modules:wishlist`.
- **The same four layers in every feature module:**
  - `domain`: entities, repository interfaces and domain errors.
  - `usecase`: one class per use case.
  - `data`: data sources and repository implementations.
  - `presentation`: controllers and DTOs.
- **Plain Kotlin inside:** `domain` and `usecase` contain no Spring code. Each module wires itself in one `@Configuration` (`CatalogModule`, `WishlistModule`).
- **Narrow module boundaries:** everything in a module is Kotlin `internal` except its `api` package. The wishlist sees only `catalog.api.ProductCatalog`.
- **Executable rules:** the boundaries are checked by Konsist tests in `app/src/test/.../ArchitectureTest.kt`.

## Consequences
- Business rules can be unit-tested with plain constructors and fakes, without a Spring context.
- Moving the catalog to a database, or extracting it into its own service, changes `data` and `ProductCatalog`'s implementation only.
- There is more structure than a two-endpoint API strictly needs. That is accepted, because it keeps each feature independent as the API grows.
