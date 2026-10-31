# 0002. Wishlists are scoped per session, by an `X-Session-Id` header

**Status:** Accepted, 2026-10-31

## Context
The wishlist is held in memory and there is no authentication. It could be one list per unique session or a single list for all users.

## Decision
There is one wishlist per session, keyed by an `X-Session-Id` header that the client generates once (a UUID) and stores on the device. A missing or malformed header is a 400 that says what to send.

## Why
- **Not a singleton:** one wishlist shared by every user is not how any shopping app behaves. Every shopper would see and edit everyone else's list.
- **A header, not a cookie (`HttpSession`):** the client is a mobile app. Native HTTP clients don't keep cookies by default, while sending a header is one line. A client-owned id also maps directly onto a user id once authentication exists; only the key changes.
- **Atomic updates:** each session's list is a `MutableStateFlow` updated with `update`, an atomic compare-and-set. Concurrent requests on one session never lose an update, and different sessions never contend.

## Consequences
- Data lives as long as the process does, and sessions are never evicted. Reads never create a session.
- A production version would back the same `WishlistRepository` interface with Redis or a database and a TTL.
