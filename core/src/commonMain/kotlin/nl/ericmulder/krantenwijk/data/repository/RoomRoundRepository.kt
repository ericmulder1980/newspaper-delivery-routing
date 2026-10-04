package nl.ericmulder.krantenwijk.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import nl.ericmulder.krantenwijk.data.db.CompletedRoundEntity
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.db.toDomain
import nl.ericmulder.krantenwijk.domain.model.ActiveRound
import nl.ericmulder.krantenwijk.domain.model.CompletedRound
import nl.ericmulder.krantenwijk.domain.repository.RoundRepository

/**
 * The active round lives in the settings DataStore (a start time and a section id, DEC-030), so it
 * survives a force-close; finished rounds live in Room. [dataStore] must be the app's single
 * settings DataStore instance (one instance per file).
 */
class RoomRoundRepository(
    db: KrantenwijkDatabase,
    private val dataStore: DataStore<Preferences>,
    private val clock: () -> Long,
) : RoundRepository {

    private val dao = db.completedRoundDao()

    override val activeRound: Flow<ActiveRound?> = dataStore.data
        .map { prefs -> prefs[STARTED_AT]?.let { ActiveRound(it, prefs[CURRENT_SEGMENT]) } }
        .distinctUntilChanged()

    override suspend fun startRound(segmentId: Long) {
        dataStore.edit {
            if (it[STARTED_AT] == null) it[STARTED_AT] = clock()
            it[CURRENT_SEGMENT] = segmentId
        }
    }

    override suspend fun setCurrentSection(segmentId: Long) {
        dataStore.edit { if (it[STARTED_AT] != null) it[CURRENT_SEGMENT] = segmentId }
    }

    override suspend fun abandonRound() {
        dataStore.edit { it.clearActive() }
    }

    override suspend fun finishRound(newspapers: Int, leaflets: Int): Long? {
        val active = activeRound.first() ?: return null
        val entity = CompletedRoundEntity(
            startedAtMillis = active.startedAtMillis,
            finishedAtMillis = clock(),
            newspapers = newspapers,
            leaflets = leaflets,
        )
        // Saved before the active round is cleared: a crash in between leaves a round that finishes again into the same row.
        val inserted = dao.insert(entity)
        val id = if (inserted > 0) inserted else checkNotNull(dao.idStartedAt(active.startedAtMillis))
        dataStore.edit { it.clearActive() }
        return id
    }

    override fun observeRound(id: Long): Flow<CompletedRound?> = dao.observe(id).map { it?.toDomain() }

    override fun observeCompletedRounds(): Flow<List<CompletedRound>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    private fun MutablePreferences.clearActive() {
        remove(STARTED_AT)
        remove(CURRENT_SEGMENT)
    }

    private companion object {
        val STARTED_AT = longPreferencesKey("round_started_at")
        val CURRENT_SEGMENT = longPreferencesKey("round_current_segment")
    }
}
