package nl.ericmulder.krantenwijk.data

import androidx.room.Room
import nl.ericmulder.krantenwijk.data.db.KrantenwijkDatabase
import nl.ericmulder.krantenwijk.data.db.buildKrantenwijk

/** In-memory database with the same driver and configuration as the app. */
fun inMemoryDatabase(): KrantenwijkDatabase = Room.inMemoryDatabaseBuilder<KrantenwijkDatabase>().buildKrantenwijk()

/** On-disk database at [path], for durability tests. */
fun fileDatabase(path: String): KrantenwijkDatabase = Room.databaseBuilder<KrantenwijkDatabase>(name = path).buildKrantenwijk()
