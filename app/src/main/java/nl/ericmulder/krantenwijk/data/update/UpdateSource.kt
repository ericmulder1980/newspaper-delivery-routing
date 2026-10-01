package nl.ericmulder.krantenwijk.data.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/** Why checking or downloading an update failed, for a clear message in Settings. */
class UpdateException(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason { OFFLINE, SERVER, NO_RELEASE, CHECKSUM, TOO_LARGE }
}

/** Finds and downloads releases (REL-C). The only network code in the app (DEC-024, NetworkUsageTest). */
interface UpdateSource {
    suspend fun latestRelease(): AvailableRelease

    /** Downloads the APK to [target] and verifies it against the published SHA-256. */
    suspend fun download(release: AvailableRelease, target: File, onProgress: (Float) -> Unit)
}

@Singleton
class GitHubUpdateSource @Inject constructor() : UpdateSource {

    override suspend fun latestRelease(): AvailableRelease = withContext(Dispatchers.IO) {
        val text = request(ReleaseSource.LATEST_URL, accept = "application/vnd.github+json") { it.bufferedReader().readText() }
        parseLatestRelease(text) ?: throw UpdateException(UpdateException.Reason.NO_RELEASE)
    }

    override suspend fun download(release: AvailableRelease, target: File, onProgress: (Float) -> Unit) = withContext(Dispatchers.IO) {
        val expected = request(release.sha256Url) { it.bufferedReader().readText() }
            .let(::parseSha256File) ?: throw UpdateException(UpdateException.Reason.NO_RELEASE)
        if (release.apkSize > MAX_APK_BYTES) throw UpdateException(UpdateException.Reason.TOO_LARGE)
        val digest = MessageDigest.getInstance("SHA-256")
        target.parentFile?.mkdirs()
        request(release.apkUrl) { input ->
            target.outputStream().use { out ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_APK_BYTES) throw UpdateException(UpdateException.Reason.TOO_LARGE)
                    digest.update(buffer, 0, read)
                    out.write(buffer, 0, read)
                    if (release.apkSize > 0) onProgress((total.toFloat() / release.apkSize).coerceAtMost(1f))
                }
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (actual != expected) {
            target.delete()
            throw UpdateException(UpdateException.Reason.CHECKSUM)
        }
    }

    /** HTTPS GET following GitHub's download redirects; maps failures to [UpdateException]. */
    private fun <T> request(url: String, accept: String? = null, read: (java.io.InputStream) -> T): T {
        require(url.startsWith("https://")) { "Only HTTPS is allowed" }
        val connection = try {
            (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Krantenwijk-Android")
                accept?.let { setRequestProperty("Accept", it) }
            }
        } catch (e: IOException) {
            throw UpdateException(UpdateException.Reason.OFFLINE, e)
        }
        try {
            val code = try {
                connection.responseCode
            } catch (e: UnknownHostException) {
                throw UpdateException(UpdateException.Reason.OFFLINE, e)
            } catch (e: IOException) {
                throw UpdateException(UpdateException.Reason.OFFLINE, e)
            }
            when {
                code == HttpURLConnection.HTTP_NOT_FOUND -> throw UpdateException(UpdateException.Reason.NO_RELEASE)
                code !in 200..299 -> throw UpdateException(UpdateException.Reason.SERVER)
            }
            return try {
                connection.inputStream.use(read)
            } catch (e: UpdateException) {
                throw e
            } catch (e: IOException) {
                throw UpdateException(UpdateException.Reason.OFFLINE, e)
            }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
        const val MAX_APK_BYTES = 50L * 1024 * 1024
    }
}
