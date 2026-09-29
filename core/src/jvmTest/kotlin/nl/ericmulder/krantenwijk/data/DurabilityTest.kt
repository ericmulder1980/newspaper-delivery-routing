package nl.ericmulder.krantenwijk.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.repository.RoomRouteRepository
import nl.ericmulder.krantenwijk.domain.model.Direction
import nl.ericmulder.krantenwijk.domain.model.Side
import nl.ericmulder.krantenwijk.domain.model.Sticker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** NFR-05: every write is committed immediately, so data survives closing (killing) the app. */
class DurabilityTest {

    @TempDir
    lateinit var dir: File

    @Test
    fun `writes survive closing and reopening the database`() = runTest {
        val path = dir.resolve("krantenwijk.db").absolutePath

        val first = fileDatabase(path)
        val repo = RoomRouteRepository(first, clock = { 0 })
        repo.saveRoute("Wijk 07", "Houten")
        val segmentId = repo.addSegment("Kerkstraat", Side.EVEN, 2, 24, Direction.ASCENDING)
        val ids = checkNotNull(repo.observeSegmentContents(segmentId).first()).addresses.take(3).map { it.id }
        repo.setSticker(ids, Sticker.NEE_JA)
        first.close()

        val second = fileDatabase(path)
        val reopened = RoomRouteRepository(second, clock = { 0 })
        val contents = checkNotNull(reopened.observeSegmentContents(segmentId).first())
        assertEquals("Wijk 07", reopened.observeRoute().first()?.name)
        assertEquals(12, contents.addresses.size)
        assertEquals(3, contents.addresses.count { it.sticker == Sticker.NEE_JA })
        second.close()
    }
}
