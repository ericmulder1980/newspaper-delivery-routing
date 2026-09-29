package nl.ericmulder.krantenwijk.domain

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * The domain layer must stay free of Android, Room and DI classes (DEC-001, DEC-013). commonMain already
 * rules out Android classes; this also catches androidx libraries such as Room that commonMain can see.
 */
class DomainPurityTest {

    @Test
    fun `domain package has no Android imports`() {
        // Unit tests run with the module directory as working directory.
        val domainDir = File("src/commonMain/kotlin/nl/ericmulder/krantenwijk/domain")
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
