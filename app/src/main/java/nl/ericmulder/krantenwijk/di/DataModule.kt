package nl.ericmulder.krantenwijk.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nl.ericmulder.krantenwijk.data.createDatabase
import nl.ericmulder.krantenwijk.data.createSettingsDataStore
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.repository.RoomRouteRepository
import nl.ericmulder.krantenwijk.data.settings.DataStoreSettingsRepository
import nl.ericmulder.krantenwijk.domain.repository.RouteRepository
import nl.ericmulder.krantenwijk.domain.repository.SettingsRepository
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

    @Provides
    @Singleton
    fun routeRepository(db: KrantenwijkDatabase): RouteRepository =
        RoomRouteRepository(db, clock = System::currentTimeMillis)

    @Provides
    @Singleton
    fun settingsRepository(dataStore: DataStore<Preferences>): SettingsRepository =
        DataStoreSettingsRepository(dataStore)
}
