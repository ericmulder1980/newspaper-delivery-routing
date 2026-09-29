package nl.ericmulder.krantenwijk.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * User settings (plan §5 "Settings (DataStore)"). Defaults follow the requirements:
 * non-existing addresses hidden (ADR-04), skipped houses shown (RND-04), screen kept on (RND-08).
 */
data class AppSettings(
    val nickname: String? = null,
    val onboardingCompleted: Boolean = false,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val showNonExisting: Boolean = false,
    val showSkipped: Boolean = true,
    val keepScreenOn: Boolean = true,
)
