package nl.ericmulder.krantenwijk.data.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A published release that this app can update to (REL-C, DEC-024). */
data class AvailableRelease(
    val versionName: String,
    val versionCode: Int,
    val apkUrl: String,
    val apkSize: Long,
    val sha256Url: String,
    /** Release notes without the machine-readable footer. */
    val notes: String,
)

/** Where releases are published (REL-B). The only host the app ever contacts. */
object ReleaseSource {
    const val OWNER = "ericmulder1980"
    const val REPO = "newspaper-delivery-routing"
    const val LATEST_URL = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"
}

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val body: String? = null,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList(),
)

@Serializable
private data class GitHubAsset(
    val name: String,
    val size: Long = 0,
    @SerialName("browser_download_url") val downloadUrl: String,
)

private val VERSION_CODE = Regex("""(?m)^versionCode:\s*(\d+)\s*$""")

/**
 * Reads GitHub's "latest release" response. Returns null when it isn't a usable Krantenwijk
 * release (draft, missing APK or checksum, or no versionCode line in the notes written by
 * prepare_release.sh).
 */
fun parseLatestRelease(text: String): AvailableRelease? {
    val release = runCatching { json.decodeFromString(GitHubRelease.serializer(), text) }.getOrNull() ?: return null
    if (release.draft || release.prerelease) return null
    val versionName = release.tagName.removePrefix("v")
    val apkName = "krantenwijk-$versionName.apk"
    val apk = release.assets.firstOrNull { it.name == apkName } ?: return null
    val sha = release.assets.firstOrNull { it.name == "$apkName.sha256" } ?: return null
    val body = release.body.orEmpty()
    val code = VERSION_CODE.find(body)?.groupValues?.get(1)?.toIntOrNull() ?: return null
    val notes = body.substringBefore("\n---").trim()
    return AvailableRelease(versionName, code, apk.downloadUrl, apk.size, sha.downloadUrl, notes)
}

/** Whether [release] is newer than the installed [currentVersionCode]. */
fun isUpdate(release: AvailableRelease, currentVersionCode: Int): Boolean = release.versionCode > currentVersionCode

/** The hash from a `.sha256` file (`<hex>  <file name>`), lower-case; null if it isn't one. */
fun parseSha256File(text: String): String? =
    text.trim().split(Regex("\\s+")).firstOrNull()?.lowercase()?.takeIf { it.matches(Regex("[0-9a-f]{64}")) }
