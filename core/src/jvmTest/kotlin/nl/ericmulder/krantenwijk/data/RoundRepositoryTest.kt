package nl.ericmulder.krantenwijk.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import nl.ericmulder.krantenwijk.data.db.CompletedRoundEntity
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.repository.RoomRoundRepository
import nl.ericmulder.krantenwijk.data.settings.DataStoreSettingsRepository
import nl.ericmulder.krantenwijk.domain.model.ActiveRound
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class RoundRepositoryTest {

    @TempDir
    lateinit var dir: File

    private lateinit var db: KrantenwijkDatabase
    private lateinit var repo: RoomRoundRepository
    private var now = 1_000L

    @BeforeEach
    fun setUp() {
        db = inMemoryDatabase()
        val dataStore = DataStoreSettingsRepository.createDataStore(dir.resolve(DataStoreSettingsRepository.FILE_NAME).absolutePath)
        repo = RoomRoundRepository(db, dataStore, clock = { now })
    }

    @AfterEach
    fun tearDown() = db.close()

    @Test
    fun `no round is active at first`() = runTest {
        assertNull(repo.activeRound.first())
        assertNull(repo.finishRound(10, 5))
    }

    @Test
    fun `start keeps the first start time and follows the section`() = runTest {
        repo.startRound(segmentId = 7)
        now = 5_000
        repo.startRound(segmentId = 8) // opening another section from Home continues the round
        repo.setCurrentSection(9)
        assertEquals(ActiveRound(startedAtMillis = 1_000, currentSegmentId = 9), repo.activeRound.first())
    }

    @Test
    fun `current section is ignored without an active round`() = runTest {
        repo.setCurrentSection(9)
        assertNull(repo.activeRound.first())
    }

    @Test
    fun `abandon discards the round`() = runTest {
        repo.startRound(7)
        repo.abandonRound()
        assertNull(repo.activeRound.first())
        assertEquals(emptyList<CompletedRound>(), repo.observeCompletedRounds().first())
    }

    @Test
    fun `finish saves start, end and counts, then clears the active round`() = runTest {
        repo.startRound(7)
        now = 1_000 + 4_335_000
        val id = repo.finishRound(newspapers = 57, leaflets = 41)!!
        assertNull(repo.activeRound.first())
        val expected = CompletedRound(id, startedAtMillis = 1_000, finishedAtMillis = 4_336_000, newspapers = 57, leaflets = 41)
        assertEquals(expected, repo.observeRound(id).first())
        assertEquals(listOf(expected), repo.observeCompletedRounds().first())
    }

    @Test
    fun `finishing a round that was already saved stores it once`() = runTest {
        // A crash after saving but before clearing the active round: the next finish must not duplicate it.
        repo.startRound(7)
        val existing = db.completedRoundDao().insert(CompletedRoundEntity(startedAtMillis = 1_000, finishedAtMillis = 9_000, newspapers = 1, leaflets = 1))
        now = 20_000
        assertEquals(existing, repo.finishRound(57, 41))
        assertEquals(1, repo.observeCompletedRounds().first().size)
        assertNull(repo.activeRound.first())
    }

    @Test
    fun `the active round survives a restart`() = runTest {
        val path = dir.resolve("restart.preferences_pb").absolutePath
        val firstProcess = CoroutineScope(Dispatchers.IO + Job())
        RoomRoundRepository(db, DataStoreSettingsRepository.createDataStore(path, firstProcess), clock = { now }).startRound(7)
        firstProcess.coroutineContext[Job]!!.cancelAndJoin()

        val reopened = DataStoreSettingsRepository.createDataStore(path, CoroutineScope(Dispatchers.IO + Job()))
        assertEquals(ActiveRound(1_000, 7), RoomRoundRepository(db, reopened, clock = { 99_000 }).activeRound.first())
    }
}
