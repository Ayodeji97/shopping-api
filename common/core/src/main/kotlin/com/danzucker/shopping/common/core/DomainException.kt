package com.danzucker.shopping.common.core

/**
 * A failure that is part of the API contract, raised by domain or use-case code.
 *
 * The domain says *what kind* of failure it is ([Kind]) and gives a stable, machine-readable
 * [code] that clients can switch on. It never names an HTTP status: mapping a [Kind] to a status is
 * the web edge's job (`common:web`'s `GlobalExceptionHandler`), which keeps the inner layers free of
 * framework types, as Clean Architecture requires.
 *
 * [message] is shown to the API client, so it must be actionable: say what was wrong *and* what to
 * do instead.
 */
abstract class DomainException(
    val kind: Kind,
    val code: String,
    override val message: String,
    /** The request parameter the failure is about, when there is one (e.g. `price_min`). */
    val field: String? = null,
) : RuntimeException(message) {
    enum class Kind {
        /** The request was malformed or out of range. Maps to 400. */
        INVALID_INPUT,

        /** A referenced resource does not exist. Maps to 404. */
        NOT_FOUND,

        /** The request conflicts with the current state. Maps to 409. */
        CONFLICT,
    }
}

/** Raised for any request value the API cannot accept. */
class InvalidInputException(
    code: String,
    message: String,
    field: String? = null,
) : DomainException(Kind.INVALID_INPUT, code, message, field)
