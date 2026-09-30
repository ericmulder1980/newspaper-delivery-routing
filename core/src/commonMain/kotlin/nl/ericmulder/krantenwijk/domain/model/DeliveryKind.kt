package nl.ericmulder.krantenwijk.domain.model

/**
 * What an address shows in the editor and in round mode. There are exactly three delivery states
 * (DEC-017: leaflets are never delivered without the newspaper) plus "does not exist".
 */
enum class DeliveryKind {
    BOTH,
    NEWSPAPER_ONLY,
    NOTHING,
    DOES_NOT_EXIST,
}
