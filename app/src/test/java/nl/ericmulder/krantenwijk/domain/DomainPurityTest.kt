package nl.ericmulder.krantenwijk.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/** The domain layer must stay pure Kotlin so its rules run as JVM unit tests (DEC-001). */
class DomainPurityTest {

    @Test
    fun `domain package has no Android imports`() {
        // Unit tests run with the module directory as working directory.
        val domainDir = File("src/main/java/nl/ericmulder/krantenwijk/domain")
        assertTrue(domainDir.isDirectory, "Domain sources not found at ${domainDir.absolutePath}")

        val forbidden = Regex("""^import\s+(android|androidx|com\.google\.android|dagger)\.""", RegexOption.MULTILINE)
        val offenders = domainDir.walkTopDown()
            .filter { it.extension == "kt" }
            .filter { forbidden.containsMatchIn(it.readText()) }
            .map { it.relativeTo(domainDir).path }
            .toList()

        assertTrue(offenders.isEmpty(), "Domain files importing Android/DI classes: $offenders")
    }
}
