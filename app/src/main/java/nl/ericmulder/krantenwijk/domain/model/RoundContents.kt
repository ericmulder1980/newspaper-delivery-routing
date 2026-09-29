package nl.ericmulder.krantenwijk.domain.model

/** What is being delivered in a round (RND-01). At least one item must be included. */
data class RoundContents(val newspaper: Boolean, val leaflets: Boolean) {
    init {
        require(newspaper || leaflets) { "A round must include the newspaper, leaflets, or both" }
    }
}
