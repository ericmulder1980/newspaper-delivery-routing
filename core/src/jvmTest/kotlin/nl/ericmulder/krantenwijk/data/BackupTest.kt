package nl.ericmulder.krantenwijk.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.backup.BackupException
import nl.ericmulder.krantenwijk.data.backup.BackupFormat
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.repository.RoomRouteRepository
import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.RouteSnapshot
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** DATA-02: export → uninstall → reinstall → import yields an identical route. */
class BackupTest {

    private lateinit var db: KrantenwijkDatabase
    private lateinit var repo: RoomRouteRepository

    @BeforeEach
    fun setUp() {
        db = inMemoryDatabase()
        repo = RoomRouteRepository(db, clock = { 1_700_000_000_000 })
    }

    @AfterEach
    fun tearDown() = db.close()

    /** A route using every feature: stickers, does not exist, exceptions, notes, additions, buildings, both directions. */
    private suspend fun buildRichRoute() {
        repo.saveRoute("Wijk 07", "Houten")
        val even = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        repo.addSegment("Kerkstraat", Side.ODD, 1, 23, Direction.DESCENDING)
        val molen = repo.addSegment("Molenweg", Side.ALL, 1, 10, Direction.ASCENDING)
        val kerk = repo.observeAllAddresses().first().filter { it.segmentId == even }
        repo.setSticker(listOf(kerk.first { it.houseNumber == 4 }.id), Sticker.NEE_JA)
        repo.setSticker(listOf(kerk.first { it.houseNumber == 8 }.id), Sticker.NEE_NEE)
        repo.setSticker(listOf(kerk.first { it.houseNumber == 6 }.id), Sticker.JA)
        repo.setExists(listOf(kerk.first { it.houseNumber == 20 }.id), false)
        repo.updateAddress(kerk.first { it.houseNumber == 2 }.copy(note = "hond · achterom", exceptionNoLeaflets = true))
        repo.deleteAddresses(listOf(kerk.first { it.houseNumber == 10 }.id))
        repo.addAddress(even, 14, "A")
        val building = repo.createBuilding(even, 12, SuffixType.LETTER, "A", "F")
        val apartments = repo.observeBuildingContents(building).first()!!.apartments
        repo.setSticker(apartments.take(2).map { it.id }, Sticker.NEE_NEE)
        repo.setExists(listOf(apartments.last().id), false)
        repo.createBuilding(molen, 4, SuffixType.NUMBER, "1", "12")
    }

    /** Strips database ids so two snapshots of the same route compare equal. */
    private fun RouteSnapshot.withoutIds(): RouteSnapshot = copy(
        route = route.copy(id = 0),
        sections = sections.map { s ->
            s.copy(
                segment = s.segment.copy(id = 0),
                addresses = s.addresses.map { it.clean() },
                buildings = s.buildings.map { b -> b.copy(building = b.building.copy(id = 0, segmentId = 0), apartments = b.apartments.map { it.clean() }) },
            )
        },
    )

    private fun Address.clean() = copy(id = 0, segmentId = 0, buildingId = null)

    @Test
    fun `export, wipe, import gives an identical route`() = runTest {
        buildRichRoute()
        val before = checkNotNull(repo.snapshot())
        val file = BackupFormat.encode(before, exportedAtMillis = 42, appVersion = "0.11.0")

        // "Uninstall and reinstall": a brand-new empty database.
        db.close()
        db = inMemoryDatabase()
        repo = RoomRouteRepository(db, clock = { 0 })
        assertNull(repo.snapshot())

        val decoded = BackupFormat.decode(file)
        assertEquals(42, decoded.exportedAtMillis)
        assertEquals("0.11.0", decoded.appVersion)
        repo.replaceAll(decoded.snapshot)

        val after = checkNotNull(repo.snapshot())
        assertEquals(before.withoutIds(), after.withoutIds())
        assertEquals(before.addressCount, after.addressCount)
    }

    @Test
    fun `restore replaces the current route`() = runTest {
        buildRichRoute()
        val backup = BackupFormat.decode(BackupFormat.encode(checkNotNull(repo.snapshot()), 0, "x")).snapshot
        repo.addSegment("Lindelaan", Side.EVEN, 2, 16, Direction.ASCENDING)
        repo.saveRoute("Renamed", null)

        repo.replaceAll(backup)

        val after = checkNotNull(repo.snapshot())
        assertEquals("Wijk 07", after.route.name)
        assertEquals(listOf("Kerkstraat", "Kerkstraat", "Molenweg"), after.sections.map { it.segment.streetName })
    }

    @Test
    fun `a failing restore leaves the current route untouched`() = runTest {
        buildRichRoute()
        val before = checkNotNull(repo.snapshot())
        // Two identical addresses in one section violate the unique index halfway through the restore.
        val broken = before.copy(
            sections = before.sections.mapIndexed { i, s -> if (i == 2) s.copy(addresses = s.addresses + s.addresses.first()) else s },
        )
        assertThrows<Exception> { repo.replaceAll(broken) }
        assertEquals(before.withoutIds(), checkNotNull(repo.snapshot()).withoutIds())
    }

    @Nested
    inner class FileFormat {
        @Test
        fun `not json or another app is not a backup`() {
            assertEquals(BackupException.Reason.NOT_A_BACKUP, assertThrows<BackupException> { BackupFormat.decode("hello") }.reason)
            val otherApp = """{"app":"com.example","formatVersion":1,"exportedAtMillis":0,"appVersion":"1","route":{"name":"x","createdAtMillis":0,"sections":[]}}"""
            assertEquals(BackupException.Reason.NOT_A_BACKUP, assertThrows<BackupException> { BackupFormat.decode(otherApp) }.reason)
        }

        @Test
        fun `a newer format is refused`() {
            val newer = """{"app":"nl.ericmulder.krantenwijk","formatVersion":99,"exportedAtMillis":0,"appVersion":"9","route":{"name":"x","createdAtMillis":0,"sections":[]}}"""
            assertEquals(BackupException.Reason.TOO_NEW, assertThrows<BackupException> { BackupFormat.decode(newer) }.reason)
        }

        @Test
        fun `invalid contents are reported as damaged`() {
            val badNumber = """{"app":"nl.ericmulder.krantenwijk","formatVersion":1,"exportedAtMillis":0,"appVersion":"1",
                "route":{"name":"Wijk","createdAtMillis":0,"sections":[{"streetName":"Kerkstraat","side":"EVEN","rangeFrom":2,"rangeTo":4,
                "direction":"ASCENDING","addresses":[{"houseNumber":-2}]}]}}"""
            assertEquals(BackupException.Reason.DAMAGED, assertThrows<BackupException> { BackupFormat.decode(badNumber) }.reason)
        }

        @Test
        fun `unknown fields from a future version are ignored`() {
            val withExtra = """{"app":"nl.ericmulder.krantenwijk","formatVersion":1,"exportedAtMillis":5,"appVersion":"1","colour":"blue",
                "route":{"name":"Wijk","createdAtMillis":0,"sections":[{"streetName":"Kerkstraat","side":"EVEN","rangeFrom":2,"rangeTo":4,
                "direction":"ASCENDING","addresses":[{"houseNumber":2,"mood":"happy"},{"houseNumber":4,"sticker":"NEE_JA"}]}]}}"""
            val snapshot = BackupFormat.decode(withExtra).snapshot
            assertEquals(2, snapshot.addressCount)
            assertEquals(Sticker.NEE_JA, snapshot.sections.single().addresses[1].sticker)
        }
    }
}
