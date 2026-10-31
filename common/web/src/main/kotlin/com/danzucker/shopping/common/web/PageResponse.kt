package com.danzucker.shopping.common.web

import com.danzucker.shopping.common.core.Page

/** The JSON envelope for every paginated endpoint. */
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Int,
    val totalPages: Int,
) {
    companion object {
        fun <T, R> from(
            page: Page<T>,
            transform: (T) -> R,
        ): PageResponse<R> = PageResponse(page.items.map(transform), page.page, page.size, page.totalItems, page.totalPages)
    }
}
