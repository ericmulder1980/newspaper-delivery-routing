package nl.ericmulder.krantenwijk.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nl.ericmulder.krantenwijk.data.BackupStorage
import nl.ericmulder.krantenwijk.data.ContentResolverBackupStorage
import nl.ericmulder.krantenwijk.data.createDatabase
import nl.ericmulder.krantenwijk.data.createSettingsDataStore
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.repository.RoomRouteRepository
import nl.ericmulder.krantenwijk.data.settings.DataStoreSettingsRepository
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): KrantenwijkDatabase = createDatabase(context)

    @Provides
    @Singleton
    fun settingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        createSettingsDataStore(context)

    /** Current time in epoch millis; injectable so tests can fix it. */
    @Provides
    @Named("clock")
    fun clock(): @JvmSuppressWildcards () -> Long = System::currentTimeMillis

    @Provides
    @Singleton
    fun routeRepository(db: KrantenwijkDatabase, @Named("clock") clock: @JvmSuppressWildcards () -> Long): RouteRepository =
        RoomRouteRepository(db, clock = clock)

    @Provides
    fun backupStorage(storage: ContentResolverBackupStorage): BackupStorage = storage

    @Provides
    @Singleton
    fun settingsRepository(dataStore: DataStore<Preferences>): SettingsRepository =
        DataStoreSettingsRepository(dataStore)
}
