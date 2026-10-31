package com.danzucker.shopping.common.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class PaginationTest {
    private val items = (1..45).toList()

    @Test
    fun `the first page holds the first size items and the totals`() {
        val page = items.paginate(PageRequest(page = 1, size = 20))

        page.items shouldBe (1..20).toList()
        page.totalItems shouldBe 45
        page.totalPages shouldBe 3
    }

    @Test
    fun `the last page holds only the remainder`() {
        items.paginate(PageRequest(page = 3, size = 20)).items shouldBe (41..45).toList()
    }

    @Test
    fun `a page past the end is empty but keeps the real totals`() {
        val page = items.paginate(PageRequest(page = 9, size = 20))

        page.items shouldBe emptyList()
        page.totalItems shouldBe 45
    }

    @Test
    fun `an empty result has zero pages`() {
        emptyList<Int>().paginate(PageRequest()).totalPages shouldBe 0
    }

    @Test
    fun `page zero is rejected because pages are 1-based`() {
        shouldThrow<InvalidInputException> { PageRequest(page = 0) }.field shouldBe "page"
    }

    @Test
    fun `a size above the maximum is rejected`() {
        shouldThrow<InvalidInputException> { PageRequest(size = 101) }.field shouldBe "size"
    }
}
