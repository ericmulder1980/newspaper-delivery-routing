package nl.ericmulder.krantenwijk

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * DEC-003 / DEC-024: the app is offline except for "Check for updates". Network APIs may only be
 * used in data/update; anything else that talks to the network fails this test.
 */
class NetworkUsageTest {

    private val networkApi = Regex("""\b(java\.net\.|javax\.net\.|HttpURLConnection|okhttp3|io\.ktor|android\.net\.http|WebView)\b""")

    @Test
    fun `network code only in the updater`() {
        val roots = listOf(File("src/main/java"), File("../core/src/commonMain/kotlin"), File("../core/src/androidMain/kotlin"))
        roots.forEach { assertTrue(it.isDirectory, "Source root not found: ${it.absolutePath}") }
        val offenders = roots.flatMap { root ->
            root.walkTopDown()
                .filter { it.extension == "kt" }
                .filter { "data/update/" !in it.invariantSeparatorsPath }
                .filter { networkApi.containsMatchIn(it.readText()) }
                .map { it.relativeTo(root).path }
                .toList()
        }
        assertTrue(offenders.isEmpty(), "Network APIs used outside data/update: $offenders")
    }
}
