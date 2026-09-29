package nl.ericmulder.krantenwijk.domain.model

/** What one address receives. */
data class Delivery(val newspaper: Boolean, val leaflets: Boolean) {
    val isNothing: Boolean get() = !newspaper && !leaflets

    companion object {
        val NOTHING = Delivery(newspaper = false, leaflets = false)
        val BOTH = Delivery(newspaper = true, leaflets = true)
        val NEWSPAPER_ONLY = Delivery(newspaper = true, leaflets = false)
    }
}
