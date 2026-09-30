package nl.ericmulder.krantenwijk.data

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Reads and writes backup files chosen with the system file picker (Storage Access Framework, no permissions). */
interface BackupStorage {
    suspend fun write(uri: String, text: String)

    suspend fun read(uri: String): String
}

@Singleton
class ContentResolverBackupStorage @Inject constructor(@ApplicationContext private val context: Context) : BackupStorage {

    override suspend fun write(uri: String, text: String) = withContext(Dispatchers.IO) {
        val stream = checkNotNull(context.contentResolver.openOutputStream(uri.toUri(), "wt")) { "Cannot open $uri" }
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
    }

    override suspend fun read(uri: String): String = withContext(Dispatchers.IO) {
        val stream = checkNotNull(context.contentResolver.openInputStream(uri.toUri())) { "Cannot open $uri" }
        stream.use { input ->
            // A route of 1,000 addresses is well under 1 MB; refuse anything absurdly large.
            // (InputStream.readNBytes needs API 33, so read with our own limit; minSdk is 31.)
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                out.write(buffer, 0, read)
                require(out.size() <= MAX_BYTES) { "File too large for a backup" }
            }
            out.toString(Charsets.UTF_8.name())
        }
    }

    private companion object {
        const val MAX_BYTES = 10 * 1024 * 1024
    }
}
