package com.danzucker.shopping.wishlist.data

import com.danzucker.shopping.wishlist.domain.SessionId
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class InMemoryWishlistRepositoryTest {
    private val repository = InMemoryWishlistRepository()
    private val session = SessionId.parse("shared-session")

    @Test
    fun `concurrent adds to one session never lose an update`() =
        runBlocking {
            val ids = (1L..500L).toList()

            ids.map { id -> async(Dispatchers.Default) { repository.add(session, id) } }.awaitAll()

            repository.observe(session).first() shouldContainExactlyInAnyOrder ids
        }

    @Test
    fun `concurrent adds of the same product succeed exactly once`() =
        runBlocking {
            val results = (1..200).map { async(Dispatchers.Default) { repository.add(session, 42) } }.awaitAll()

            results.count { it } shouldBe 1
            repository.observe(session).first() shouldBe listOf(42L)
        }

    @Test
    fun `reading an unknown session returns an empty wishlist`() =
        runBlocking {
            repository.observe(SessionId.parse("nobody")).first() shouldBe emptyList()
        }
}
