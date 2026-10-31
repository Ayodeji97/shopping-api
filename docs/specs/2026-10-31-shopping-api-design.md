# Shopping Wishlist & Product Discovery API — Design Spec

> Status: implemented. Revision 2026-10-31 (during build): the layers were renamed to the spec's own Clean Architecture vocabulary (`domain` / `usecase` / `data` / `presentation`). `domain` and `usecase` were also made framework-free: no Spring annotations, with each module wired in one `@Configuration` (`CatalogModule`, `WishlistModule`) and the rule enforced by Konsist.

## Context
Take-home assessment: a Kotlin backend REST API for product browsing, search/filter, and a per-user in-memory wishlist, built with Clean Architecture, DI, Coroutines/Flow, docs, and unit tests. The user wants a **spec-driven** approach and a **modular monolith** that mirrors their existing `moyi-backend` project (`~/Desktop/Learning/project_base_learning/Gratitude/moyi-backend`) conventions. The target directory `~/Desktop/shopping_app/Shopping_App` currently holds only a bare Kotlin JVM Gradle scaffold (`build.gradle.kts`, `src/main/kotlin/Main.kt`) and is not a git repo.


## Decisions (from the user)
| Topic | Decision |
|---|---|
| Wishlist scope | Per session via `X-Session-Id` header; missing/invalid → 400. Justified in KDoc on the store + README |
| Wishlist conflicts | Duplicate add → **409**; remove item not in wishlist → **404**; unknown product on add → **404** |
| Physical structure | Gradle multi-module like Moyi, with `build-logic` convention plugins |
| Async | Spring MVC + `suspend` controllers/use cases; repositories expose `Flow` |
| Stack | Spring Boot 4.1 / Kotlin 2.4 (Moyi catalog versions), **JDK 21** toolchain |
| Pagination | `?page=1&size=20` (1-based, default size 20, max 100) → `{items, page, size, totalItems, totalPages}` |
| Search | `/products/search` is paginated **and** accepts the same filters |
| Invalid input | Always **400**, RFC 9457 ProblemDetail body with `code` + optional `errors[]` |

## My calls (flag if you disagree)
- Brand/category filter = exact match, **case-insensitive** (mobile users type "nike"). Search = case-insensitive substring on name OR description; blank `query` → 400.
- Product id = `Long`; non-numeric id → 400 `INVALID_PARAMETER`.
- `price` = `BigDecimal` serialized as JSON number; `price_min`/`price_max` ≥ 0, `min > max` → 400.
- Session id: 1–64 chars `[A-Za-z0-9_-]` (UUID expected by convention).
- `POST /wishlist/{id}` → 201 with `{productIds}`; `DELETE` → 204; `GET /wishlist` → 200 `{productIds}` in insertion order (empty list for a new session).
- Results sorted by id for stable pagination. Page beyond the end → 200 with empty `items`.
- No Testcontainers/JPA/security/Actuator beyond `/actuator/health`; springdoc kept (Swagger UI is useful for a reviewer). No session eviction — noted as a known limitation in README.
- Keep JaCoCo/detekt/ktlint from Moyi conventions; drop the 80% coverage gate to non-blocking report (assessment scope).

## Module layout
```
Shopping_App/
  build-logic/            kotlin-common-convention, spring-boot-service-convention, spring-boot-app-convention
  gradle/libs.versions.toml   (copied/trimmed from moyi-backend)
  common/core             ProductId, SessionId value classes; Clock not needed
  common/web              ApiException, ErrorCode, ProblemDetails, GlobalExceptionHandler, PageResponse
  modules/catalog         api/ (ProductCatalog port) · domain/ · usecase/ · data/ (in-memory repo + seed) · presentation/ · CatalogModule (DI)
  modules/wishlist        domain/ · usecase/ · data/ (in-memory session store) · presentation/ · WishlistModule (DI)
  app                     @SpringBootApplication, application.yml, Konsist ArchitectureTest, MockMvc tests
  docs/specs/…, adr/0001-modular-monolith.md, adr/0002-wishlist-session-scope.md, README.md
```
Package root `com.danzucker.shopping`. Everything except each module's `api` is Kotlin `internal` (Moyi pattern). Wishlist depends only on `catalog.api.ProductCatalog` (`suspend fun exists(id: ProductId): Boolean`) to validate products.

## Layers per module (Clean Architecture mapping)
- **domain**: two product models. `Product` is the list model (id, name, description, imageUrl, price), used by list and search. `ProductDetails` is the full record (all fields, including brand and category; `require` invariants), used by `GET /products/{id}`, with `toProduct()`. *Revision 2026-10-31: the list must not carry brand or category, because the UI does not show them there.* `ProductQuery` (text?, brand?, category?, priceMin?, priceMax?, page, size — validates `min ≤ max`), `Page<T>`; `Wishlist` (ordered set; `add` throws `AlreadyInWishlist`, `remove` throws `NotInWishlist`).
- **repository ports** (domain): `ProductRepository { fun observeAll(): Flow<Product>; suspend fun findById(id): Product? }`; `WishlistRepository { suspend fun get(session): Wishlist; suspend fun save(session, wishlist) }`.
- **data sources** (`data`): `InMemoryProductRepository` seeded from `SeedProducts` (12 diverse products across Electronics, Footwear, Apparel, Home, Beauty, Books; brands like Apple, Sony, Nike, Adidas, Dyson, IKEA, …; picsum/placeholder image URLs). `InMemoryWishlistRepository` = `ConcurrentHashMap<SessionId, MutableStateFlow<List<Long>>>`, with atomic `update` (CAS) for add/remove — **the session-scope justification KDoc lives here**.
- **use cases** (`usecase`, verb-named plain classes, `suspend operator fun invoke`, no framework imports): `ListProducts`, `GetProductDetails`, `SearchProducts`, `GetWishlist`, `AddToWishlist`, `RemoveFromWishlist`. Filtering/pagination built on `Flow` (`filter` → `map(toProduct)` → `toList` → paginate) in `ListProducts`, which `SearchProducts` reuses.
- **presentation**: `ProductsController` (`GET /products`, `/products/{id}`, `/products/search`), `WishlistController` (`GET /wishlist`, `POST|DELETE /wishlist/{productId}`), DTOs `ProductResponse` (from `Product`), `ProductDetailsResponse` (from `ProductDetails`), `WishlistResponse`. KDoc on every endpoint (purpose, params, statuses).
- **DI**: Spring, via one `@Configuration` per module that constructs every bean; constructor injection only.

## Error handling (`common/web`, adapted from moyi `com.moyi.common.web`)
`ApiException(status, ErrorCode, detail, errors?)` subclasses: `ProductNotFoundException` (404 `PRODUCT_NOT_FOUND`), `AlreadyInWishlistException` (409 `ALREADY_IN_WISHLIST`), `NotInWishlistException` (404 `NOT_IN_WISHLIST`), `InvalidRequestException` (400 `INVALID_PARAMETER`/`MISSING_SESSION_ID`). `GlobalExceptionHandler : ResponseEntityExceptionHandler` maps type mismatch, missing header/param, constraint violations → 400; unknown → 500 `INTERNAL_ERROR`. Messages actionable, e.g. `"price_min (50) must not exceed price_max (10)"`, `"Send an X-Session-Id header (e.g. a UUID) to identify your wishlist"`.

## Testing
- **Unit tests (use cases, ≥ deliverable's 2)**: `ListProductsTest` (filters combine, case-insensitive, price range, pagination math, empty page), `SearchProductsTest`, `AddToWishlistTest` (adds, 409 duplicate, 404 unknown product, session isolation), `RemoveFromWishlistTest`. Fakes not mocks (`FakeProductCatalog`), kotest `shouldBe`, `kotlinx-coroutines-test` `runTest`, backtick names.
- **Domain tests**: `ProductQueryTest`, `WishlistTest`.
- **Web tests** (`app`): `@SpringBootTest` + MockMvc with `asyncDispatch` (required for suspend handlers) covering each endpoint's happy path + 400/404/409 bodies.
- **Concurrency test**: 100 parallel adds of distinct products to one session → all present.
- **Konsist `ArchitectureTest`**: layers depend inward; controllers only in `web`; wishlist only imports `catalog.api`.

## Build order (each step = one commit, Conventional Commits)
1. Spec doc + git init + `.gitignore`.
2. Gradle: settings, version catalog, build-logic conventions, module skeletons; `./gradlew build` green.
3. `common:core` + `common:web` (errors, ProblemDetails, PageResponse).
4. Catalog domain + repo + seed + use cases + tests (TDD).
5. Catalog web + MockMvc tests.
6. Wishlist domain + store + use cases + tests (TDD).
7. Wishlist web + MockMvc tests + concurrency test.
8. Konsist architecture test, springdoc, actuator health.
9. Bruno collection (see below).
10. README (architecture + Mermaid diagram, run, every endpoint with curl request/response examples, session-scope justification, limitations), ADRs.

## Bruno collection (`tools/bruno/`, same format as moyi-backend/tools/bruno)
Shows every endpoint, happy and error paths, and doubles as a runnable smoke test (Bruno app Runner, or `npx @usebruno/cli run --env local`).
- `bruno.json` (name "Shopping API"), `environments/local.bru`: `baseUrl: http://localhost:8080`, `otherSessionId: demo-bob`.
- Folders, numbered so the Runner executes in story order:
  - **01 Products**: list (default 20), page 2 with size 5, get by id, filter by brand, by category, by price range, all filters combined.
  - **02 Search**: search "wireless", search + category filter, paginated search.
  - **03 Wishlist** (a "Start session" request's pre-request script sets `sessionId = "demo-" + Date.now()` so the collection reruns cleanly): empty wishlist → add product → add second → get (2 ids, in order) → add duplicate (409) → remove → remove again (404) → other session is still empty (isolation).
  - **04 Errors**: product 999 (404), id `abc` (400), `price_min > price_max` (400), size 500 (400), blank search query (400), wishlist without `X-Session-Id` (400), add unknown product (404).
- Every request has a `docs {}` block (what it shows, expected status) and `assert`/`tests` blocks checking status plus key body fields (e.g. `res.body.code`).
- Paths match the spec exactly (`/products`, `/wishlist`) — no `/api/v1` prefix, unlike Moyi, so the reviewer sees the spec's URLs.
- README links to the collection: "Open `tools/bruno` in Bruno, pick the `local` environment, Run collection."

## Verification
- `./gradlew clean build` — compiles, detekt/ktlint pass, all tests green (report actual output).
- `./gradlew :app:bootRun`, then curl: `/products`, `/products?brand=nike&price_min=50&page=1&size=5`, `/products/1`, `/products/999` (404), `/products/abc` (400), `/products/search?query=wireless`, `/products?price_min=50&price_max=10` (400); wishlist add/dup(409)/get/delete/delete-again(404)/no-header(400); two different session ids stay isolated.
- `/swagger-ui.html` and `/actuator/health` respond.
- `npx @usebruno/cli run --env local` from `tools/bruno` against the running app: every request's assertions pass, and a second run also passes because each run uses a fresh session.
