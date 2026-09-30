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
import nl.ericmulder.krantenwijk.domain.repository.BuildingConflictException
import nl.ericmulder.krantenwijk.domain.repository.DuplicateAddressException
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
            val duplicate = assertThrows<DuplicateAddressException> { repo.addAddress(id, 14, " A ") }
            assertEquals(14, duplicate.houseNumber)
            assertEquals("A", duplicate.addition)
            assertThrows<DuplicateAddressException> { repo.addAddress(id, 12, null) }
            assertThrows<DuplicateAddressException> { repo.addAddress(id, 12, "  ") }
        }

        @Test
        fun `same number in another segment is allowed`() = runTest {
            val even = kerkstraatEven()
            val other = repo.addSegment("Molenweg", Side.ALL, 1, 3, Direction.ASCENDING)
            repo.addAddress(other, 12, null)
            assertEquals(12, contents(even).addresses.size)
        }

        @Test
        fun `all addresses of the route, including apartments`() = runTest {
            val even = kerkstraatEven()
            repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
            repo.createBuilding(even, 12, SuffixType.LETTER, "A", "C")
            val all = repo.observeAllAddresses().first()
            assertEquals(11 + 3 + 10, all.size)
            assertEquals(3, all.count { it.buildingId != null })
        }

        @Test
        fun `address counts exclude does not exist`() = runTest {
            val even = kerkstraatEven()
            val other = repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
            repo.setExists(contents(even).addresses.take(2).map { it.id }, false)
            assertEquals(mapOf(even to 10, other to 10), repo.observeAddressCounts().first())
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
    inner class CreateBuilding {
        @Test
        fun `number 12 becomes a building with apartments A to L`() = runTest {
            val id = kerkstraatEven()
            val buildingId = repo.createBuilding(id, 12, SuffixType.LETTER, "a", "L")

            val c = contents(id)
            val building = c.buildings.single()
            assertEquals(buildingId, building.id)
            assertEquals("", building.separator)
            val apartments = c.addresses.filter { it.buildingId == buildingId }
            assertEquals(('A'..'L').map(Char::toString), apartments.map { it.addition })
            assertTrue(c.addresses.none { it.houseNumber == 12 && it.buildingId == null })
            // Apartments count as addresses: 11 houses + 12 apartments.
            assertEquals(23, repo.observeAddressCounts().first()[id])
        }

        @Test
        fun `numeric units use a hyphen`() = runTest {
            val id = kerkstraatEven()
            repo.createBuilding(id, 14, SuffixType.NUMBER, "1", "20")
            val building = contents(id).buildings.single()
            assertEquals("-", building.separator)
            val suffixes = contents(id).addresses.filter { it.buildingId == building.id }.map { it.addition }
            assertEquals((1..20).map(Int::toString), suffixes)
        }

        @Test
        fun `apartments sort naturally in walking order`() = runTest {
            val id = kerkstraatEven()
            repo.createBuilding(id, 14, SuffixType.NUMBER, "1", "10")
            assertEquals(
                listOf("12", "14-1", "14-2", "14-3"),
                contents(id).addresses.map { a -> "${a.houseNumber}${if (a.buildingId != null) "-" else ""}${a.addition.orEmpty()}" }
                    .subList(5, 9),
            )
        }

        @Test
        fun `conflicting standalone addition is reported and nothing changes`() = runTest {
            val id = kerkstraatEven()
            repo.addAddress(id, 12, "C")
            val error = assertThrows<BuildingConflictException> { repo.createBuilding(id, 12, SuffixType.LETTER, "A", "L") }
            assertEquals(listOf("12C"), error.labels)
            assertEquals(0, contents(id).buildings.size)
            assertTrue(contents(id).addresses.any { it.houseNumber == 12 && it.addition == null })
        }

        @Test
        fun `invalid unit range is rejected`() = runTest {
            val id = kerkstraatEven()
            assertThrows<IllegalArgumentException> { repo.createBuilding(id, 12, SuffixType.LETTER, "L", "A") }
            assertEquals(0, contents(id).buildings.size)
        }

        @Test
        fun `no longer a building restores the plain number`() = runTest {
            val id = kerkstraatEven()
            val buildingId = repo.createBuilding(id, 12, SuffixType.LETTER, "A", "C")
            repo.removeBuilding(buildingId)

            val c = contents(id)
            assertEquals(0, c.buildings.size)
            assertEquals(12, c.addresses.size)
            assertTrue(c.addresses.any { it.houseNumber == 12 && it.addition == null && it.buildingId == null })
        }
    }

    @Nested
    inner class BuildingContentsAndApartments {
        @Test
        fun `building contents list apartments naturally with the street name`() = runTest {
            val id = kerkstraatEven()
            val buildingId = repo.createBuilding(id, 14, SuffixType.NUMBER, "1", "12")
            val c = checkNotNull(repo.observeBuildingContents(buildingId).first())
            assertEquals("Kerkstraat", c.streetName)
            assertEquals((1..12).map(Int::toString), c.apartments.map { it.addition })
        }

        @Test
        fun `add apartment 12M after 12L (BLD-05)`() = runTest {
            val id = kerkstraatEven()
            val buildingId = repo.createBuilding(id, 12, SuffixType.LETTER, "A", "L")
            repo.addApartment(buildingId, " m ")
            val apartments = checkNotNull(repo.observeBuildingContents(buildingId).first()).apartments
            assertEquals("M", apartments.last().addition)
            assertEquals(buildingId, apartments.last().buildingId)
            assertEquals(13, apartments.size)
        }

        @Test
        fun `invalid or duplicate apartment is rejected`() = runTest {
            val id = kerkstraatEven()
            val letters = repo.createBuilding(id, 12, SuffixType.LETTER, "A", "C")
            val numbers = repo.createBuilding(id, 14, SuffixType.NUMBER, "1", "3")
            assertThrows<IllegalArgumentException> { repo.addApartment(letters, "AB") }
            assertThrows<IllegalArgumentException> { repo.addApartment(letters, "4") }
            assertThrows<IllegalArgumentException> { repo.addApartment(numbers, "0") }
            assertThrows<DuplicateAddressException> { repo.addApartment(letters, "b") }
            assertThrows<DuplicateAddressException> { repo.addApartment(numbers, "02") }
        }

        @Test
        fun `contents end when the building is removed`() = runTest {
            val id = kerkstraatEven()
            val buildingId = repo.createBuilding(id, 12, SuffixType.LETTER, "A", "C")
            repo.removeBuilding(buildingId)
            assertNull(repo.observeBuildingContents(buildingId).first())
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
