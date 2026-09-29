package nl.ericmulder.krantenwijk.domain.model

/** Which house numbers a street section covers. */
enum class Side {
    EVEN,
    ODD,
    ALL,
    ;

    fun matches(number: Int): Boolean = when (this) {
        EVEN -> number % 2 == 0
        ODD -> number % 2 != 0
        ALL -> true
    }
}
