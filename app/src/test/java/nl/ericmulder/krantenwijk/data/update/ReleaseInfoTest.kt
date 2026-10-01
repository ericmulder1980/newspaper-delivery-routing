package nl.ericmulder.krantenwijk.data.update

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReleaseInfoTest {

    /** Shape of the real v0.11.0 response from api.github.com (trimmed). */
    private fun release(
        tag: String = "v0.11.0",
        body: String = "Krantenwijk 0.11.0 for Android 12 and newer.\n\n### Changes\n- DATA-A\n\n---\nversionCode: 12\nsha256: abc",
        assets: String = """
            {"name":"krantenwijk-0.11.0.apk","size":7724961,"browser_download_url":"https://github.com/x/releases/download/v0.11.0/krantenwijk-0.11.0.apk"},
            {"name":"krantenwijk-0.11.0.apk.sha256","size":89,"browser_download_url":"https://github.com/x/releases/download/v0.11.0/krantenwijk-0.11.0.apk.sha256"}
        """,
        draft: Boolean = false,
    ) = """{"tag_name":"$tag","name":"Krantenwijk","draft":$draft,"prerelease":false,"body":${quote(body)},"assets":[$assets],"author":{"login":"x"}}"""

    private fun quote(s: String) = "\"" + s.replace("\n", "\\n") + "\""

    @Test
    fun `parses version, assets and notes`() {
        val r = parseLatestRelease(release())!!
        assertEquals("0.11.0", r.versionName)
        assertEquals(12, r.versionCode)
        assertEquals(7724961, r.apkSize)
        assertTrue(r.apkUrl.endsWith("krantenwijk-0.11.0.apk"))
        assertTrue(r.sha256Url.endsWith(".apk.sha256"))
        assertEquals("Krantenwijk 0.11.0 for Android 12 and newer.\n\n### Changes\n- DATA-A", r.notes)
    }

    @Test
    fun `parses the real v0_11_0 response from GitHub`() {
        val text = checkNotNull(javaClass.getResource("/update/latest-v0.11.0.json")).readText()
        val r = parseLatestRelease(text)!!
        assertEquals("0.11.0", r.versionName)
        assertEquals(12, r.versionCode)
        assertEquals(
            "https://github.com/ericmulder1980/newspaper-delivery-routing/releases/download/v0.11.0/krantenwijk-0.11.0.apk",
            r.apkUrl,
        )
        assertTrue(r.notes.startsWith("Krantenwijk 0.11.0 for Android 12 and newer."))
    }

    @Test
    fun `unusable releases are ignored`() {
        assertNull(parseLatestRelease("not json"))
        assertNull(parseLatestRelease(release(draft = true)))
        assertNull(parseLatestRelease(release(body = "no version code here")))
        assertNull(parseLatestRelease(release(assets = """{"name":"other.zip","browser_download_url":"https://x"}""")))
        // APK without its checksum file
        assertNull(parseLatestRelease(release(assets = """{"name":"krantenwijk-0.11.0.apk","browser_download_url":"https://x"}""")))
    }

    @Test
    fun `newer only when the version code is higher`() {
        val r = parseLatestRelease(release())!!
        assertTrue(isUpdate(r, currentVersionCode = 11))
        assertFalse(isUpdate(r, currentVersionCode = 12))
        assertFalse(isUpdate(r, currentVersionCode = 13))
    }

    @Test
    fun `sha256 file is parsed`() {
        val hex = "f7613508180cb85d239ed93d1b53b6bc35dabc2585a890173150b163c9cf9a0c"
        assertEquals(hex, parseSha256File("${hex.uppercase()}  krantenwijk-0.11.0.apk\n"))
        assertNull(parseSha256File("hello"))
    }
}
