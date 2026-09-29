package nl.ericmulder.krantenwijk.data

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.db.BuildingEntity
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.db.toEntity
import nl.ericmulder.krantenwijk.data.repository.RoomRouteRepository
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RouteRepositoryTest {

    private lateinit var db: KrantenwijkDatabase
    private lateinit var repo: RoomRouteRepository

    @BeforeEach
    fun setUp() {
        db = inMemoryDatabase()
        repo = RoomRouteRepository(db, clock = { 1_000L })
    }

    @AfterEach
    fun tearDown() = db.close()

    private suspend fun contents(segmentId: Long) = checkNotNull(repo.observeSegmentContents(segmentId).first())

    private suspend fun labels(segmentId: Long) =
        contents(segmentId).addresses.map { "${it.houseNumber}${it.addition.orEmpty()}" }

    private suspend fun kerkstraatEven(): Long {
        repo.saveRoute("Wijk 07", "Houten")
        return repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
    }

    @Nested
    inner class Route {
        @Test
        fun `save creates the route, then renames it`() = runTest {
            assertNull(repo.observeRoute().first())
            repo.saveRoute(" Wijk 07 ", " Houten ")
            repo.saveRoute("Wijk 8", "")

            val route = checkNotNull(repo.observeRoute().first())
            assertEquals("Wijk 8", route.name)
            assertNull(route.town)
            assertEquals(1_000L, route.createdAtMillis)
            assertEquals(1, db.routeDao().let { listOfNotNull(it.get()) }.size)
        }

        @Test
        fun `blank name is rejected`() = runTest {
            assertThrows<IllegalArgumentException> { repo.saveRoute("  ", null) }
        }
    }

    @Nested
    inner class Segments {
        @Test
        fun `adding a segment generates its addresses`() = runTest {
            val id = kerkstraatEven()
            assertEquals((2..24 step 2).map(Int::toString), labels(id))
            assertTrue(contents(id).addresses.all { it.sticker == Sticker.NONE && it.exists && it.segmentId == id })
        }

        @Test
        fun `segment needs a route`() = runTest {
            assertThrows<IllegalStateException> { repo.addSegment("Kerkstraat", Side.ALL, 1, 5, Direction.ASCENDING) }
        }

        @Test
        fun `range entered high to low is rejected and nothing is stored`() = runTest {
            repo.saveRoute("Wijk 07", null)
            assertThrows<IllegalArgumentException> { repo.addSegment("Kerkstraat", Side.ODD, 23, 1, Direction.ASCENDING) }
            assertEquals(emptyList<Any>(), repo.observeSegments().first())
        }

        @Test
        fun `segments are appended in walking order`() = runTest {
            val a = kerkstraatEven()
            val b = repo.addSegment("Kerkstraat", Side.ODD, 1, 23, Direction.DESCENDING)
            val c = repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
            assertEquals(listOf(a to 0, b to 1, c to 2), repo.observeSegments().first().map { it.id to it.position })
        }

        @Test
        fun `reorder persists and must list every segment`() = runTest {
            val a = kerkstraatEven()
            val b = repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
            repo.reorderSegments(listOf(b, a))
            assertEquals(listOf(b, a), repo.observeSegments().first().map { it.id })
            assertThrows<IllegalArgumentException> { repo.reorderSegments(listOf(a)) }
        }

        @Test
        fun `direction changes the walking order of addresses`() = runTest {
            val id = kerkstraatEven()
            repo.setDirection(id, Direction.DESCENDING)
            assertEquals((2..24 step 2).reversed().map(Int::toString), labels(id))
        }

        @Test
        fun `deleting a segment removes its addresses and closes the gap`() = runTest {
            val a = kerkstraatEven()
            val b = repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
            val c = repo.addSegment("Lindelaan", Side.EVEN, 2, 16, Direction.ASCENDING)
            val kerkAddress = contents(a).addresses.first().id

            repo.deleteSegment(b)

            assertEquals(listOf(a to 0, c to 1), repo.observeSegments().first().map { it.id to it.position })
            assertNull(repo.observeSegmentContents(b).first())
            assertEquals(0, db.addressDao().observeForSegment(b).first().size)
            assertTrue(db.addressDao().get(kerkAddress) != null)
        }

        @Test
        fun `street names are distinct and sorted`() = runTest {
            kerkstraatEven()
            repo.addSegment("Kerkstraat", Side.ODD, 1, 23, Direction.DESCENDING)
            repo.addSegment("molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
            assertEquals(listOf("Kerkstraat", "molenweg"), repo.observeStreetNames().first())
        }
    }

    @Nested
    inner class Addresses {
        @Test
        fun `add numbers with additions and keep them sorted`() = runTest {
            val id = kerkstraatEven()
            repo.addAddress(id, 14, "B")
            repo.addAddress(id, 14, " A ")
            repo.addAddress(id, 26, null)
            repo.deleteAddresses(contents(id).addresses.filter { it.houseNumber == 10 }.map { it.id })

            assertEquals(
                listOf("2", "4", "6", "8", "12", "14", "14A", "14B", "16", "18", "20", "22", "24", "26"),
                labels(id),
            )
        }

        @Test
        fun `duplicate number in the same segment is rejected`() = runTest {
            val id = kerkstraatEven()
            repo.addAddress(id, 14, "A")
            assertThrows<Exception> { repo.addAddress(id, 14, "A") }
            assertThrows<Exception> { repo.addAddress(id, 12, null) }
            assertThrows<Exception> { repo.addAddress(id, 12, "  ") }
        }

        @Test
        fun `bulk sticker updates exactly the selected addresses`() = runTest {
            val id = kerkstraatEven()
            val selected = contents(id).addresses.take(5).map { it.id }
            repo.setSticker(selected, Sticker.NEE_NEE)

            val bySticker = contents(id).addresses.groupBy({ it.sticker }, { it.id })
            assertEquals(selected, bySticker[Sticker.NEE_NEE])
            assertEquals(7, bySticker[Sticker.NONE]?.size)
        }

        @Test
        fun `mark and unmark does not exist`() = runTest {
            val id = kerkstraatEven()
            val twenty = contents(id).addresses.single { it.houseNumber == 20 }.id
            repo.setExists(listOf(twenty), false)
            assertEquals(false, db.addressDao().get(twenty)?.exists)
            repo.setExists(listOf(twenty), true)
            assertEquals(true, db.addressDao().get(twenty)?.exists)
        }

        @Test
        fun `update stores note and exceptions`() = runTest {
            val id = kerkstraatEven()
            val address = contents(id).addresses.first()
            repo.updateAddress(address.copy(note = "dog", exceptionNoLeaflets = true))
            val stored = contents(id).addresses.first()
            assertEquals("dog", stored.note)
            assertEquals(true, stored.exceptionNoLeaflets)
        }

        @Test
        fun `contents update reactively`() = runTest {
            val id = kerkstraatEven()
            repo.observeSegmentContents(id).test {
                assertEquals(12, awaitItem()?.addresses?.size)
                repo.addAddress(id, 26, null)
                assertEquals(13, awaitItem()?.addresses?.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class Buildings {
        @Test
        fun `apartments belong to a building and are deleted with it`() = runTest {
            val id = kerkstraatEven()
            repo.deleteAddresses(contents(id).addresses.filter { it.houseNumber == 12 }.map { it.id })
            val buildingId = db.buildingDao().insert(
                BuildingEntity(segmentId = id, houseNumber = 12, name = null, suffixType = SuffixType.LETTER, separator = ""),
            )
            db.addressDao().insertAll(
                listOf("A", "B", "C").map { Address(12, it, segmentId = id, buildingId = buildingId).toEntity() },
            )
            assertEquals(1, contents(id).buildings.size)
            assertEquals(listOf("12A", "12B", "12C"), labels(id).filter { it.startsWith("12") })

            db.buildingDao().delete(buildingId)

            assertEquals(emptyList<String>(), labels(id).filter { it.startsWith("12") })
            assertEquals(0, contents(id).buildings.size)
        }
    }
}
