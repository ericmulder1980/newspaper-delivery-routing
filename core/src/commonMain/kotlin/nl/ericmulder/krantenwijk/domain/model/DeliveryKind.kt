package nl.ericmulder.krantenwijk.domain.model

/** What an address shows in the editor and in round mode: the four delivery states plus "does not exist". */
enum class DeliveryKind {
    BOTH,
    NEWSPAPER_ONLY,
    LEAFLETS_ONLY,
    NOTHING,
    DOES_NOT_EXIST,
}
