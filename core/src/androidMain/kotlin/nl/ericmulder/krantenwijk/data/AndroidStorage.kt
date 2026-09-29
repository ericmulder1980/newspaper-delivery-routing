package nl.ericmulder.krantenwijk.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.db.buildKrantenwijk
import nl.ericmulder.krantenwijk.data.settings.DataStoreSettingsRepository

/** Opens the app's database in its private storage (excluded from backups, DEC-011). */
fun createDatabase(context: Context): KrantenwijkDatabase {
    val appContext = context.applicationContext
    return Room.databaseBuilder<KrantenwijkDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(KrantenwijkDatabase.FILE_NAME).absolutePath,
    ).buildKrantenwijk()
}

/** Creates the settings DataStore in the app's private files directory. */
fun createSettingsDataStore(context: Context): DataStore<Preferences> =
    DataStoreSettingsRepository.createDataStore(
        context.applicationContext.filesDir.resolve(DataStoreSettingsRepository.FILE_NAME).absolutePath,
    )
