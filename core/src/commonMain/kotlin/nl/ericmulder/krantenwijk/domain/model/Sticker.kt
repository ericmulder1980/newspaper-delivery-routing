package nl.ericmulder.krantenwijk.domain.model

/**
 * The sticker physically on the mailbox, stored as observed (DEC-004).
 * What gets delivered is derived from it in [nl.ericmulder.krantenwijk.domain.rules.deliveryFor].
 */
enum class Sticker {
    NONE,
    JA,
    NEE_JA,
    NEE_NEE,
}
