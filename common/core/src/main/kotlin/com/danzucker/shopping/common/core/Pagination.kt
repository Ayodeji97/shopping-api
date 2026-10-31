package com.danzucker.shopping.common.core

/**
 * A 1-based page request. Validated on construction, so an invalid page can never reach a
 * use case.
 */
data class PageRequest(
    val page: Int = 1,
    val size: Int = DEFAULT_SIZE,
) {
    init {
        if (page < 1) {
            throw InvalidInputException("INVALID_PAGE", "page must be 1 or greater (pages are 1-based); got $page.", "page")
        }
        if (size !in 1..MAX_SIZE) {
            throw InvalidInputException("INVALID_PAGE_SIZE", "size must be between 1 and $MAX_SIZE; got $size.", "size")
        }
    }

    /** Index of the first item on this page, in the full result list. */
    val offset: Long get() = (page - 1).toLong() * size

    companion object {
        /** The spec's default page size. */
        const val DEFAULT_SIZE = 20

        /** Upper bound so one request cannot ask for an unbounded page. */
        const val MAX_SIZE = 100
    }
}

/** One page of results plus what a client needs to page through the rest. */
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Int,
) {
    val totalPages: Int get() = if (totalItems == 0) 0 else (totalItems + size - 1) / size

    fun <R> map(transform: (T) -> R): Page<R> = Page(items.map(transform), page, size, totalItems)
}

/**
 * Cuts this list to the requested page. A page past the end is not an error: it is an empty
 * page with the real totals, so a client can tell it has gone too far.
 */
fun <T> List<T>.paginate(request: PageRequest): Page<T> {
    val from = request.offset.coerceAtMost(size.toLong()).toInt()
    val to = (from + request.size).coerceAtMost(size)
    return Page(subList(from, to), request.page, request.size, size)
}
