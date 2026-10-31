package com.danzucker.shopping.wishlist.domain

import com.danzucker.shopping.common.core.InvalidInputException

/**
 * Identifies whose wishlist a request is about. The client sends it in the `X-Session-Id` header.
 *
 * Anonymous by design (there is no authentication): a mobile app generates a UUID on
 * first launch, stores it, and sends it on every wishlist call. Restricting it to a short, safe
 * alphabet keeps it from being used to inject content into logs.
 */
@JvmInline
value class SessionId private constructor(
    val value: String,
) {
    companion object {
        const val HEADER = "X-Session-Id"
        private const val MAX_LENGTH = 64
        private val ALLOWED = Regex("^[A-Za-z0-9_-]{1,$MAX_LENGTH}$")

        /** Validates a raw header value. A missing or malformed id is a 400 that says how to fix it. */
        fun parse(raw: String?): SessionId {
            if (raw.isNullOrBlank()) {
                throw InvalidInputException(
                    "MISSING_SESSION_ID",
                    "Send an $HEADER header to identify your wishlist, e.g. a UUID generated once per app install.",
                    HEADER,
                )
            }
            if (!ALLOWED.matches(raw)) {
                throw InvalidInputException(
                    "INVALID_SESSION_ID",
                    "$HEADER must be 1-$MAX_LENGTH characters of letters, digits, '-' or '_' (a UUID works).",
                    HEADER,
                )
            }
            return SessionId(raw)
        }
    }
}
