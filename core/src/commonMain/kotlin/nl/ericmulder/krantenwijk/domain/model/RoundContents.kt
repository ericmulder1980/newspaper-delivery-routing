package nl.ericmulder.krantenwijk.domain.model

/**
 * What is delivered in a round. The newspaper is always included; leaflets vary per week and are
 * chosen at round start (RND-01, DEC-017).
 */
data class RoundContents(val leaflets: Boolean) {
    val newspaper: Boolean get() = true
}
