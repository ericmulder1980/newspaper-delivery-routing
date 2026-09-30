package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Delivery
import nl.ericmulder.krantenwijk.domain.model.DeliveryKind
import nl.ericmulder.krantenwijk.domain.model.RoundContents
import nl.ericmulder.krantenwijk.domain.model.Sticker

/**
 * The mailbox sticker table (plan §3.3). This is the only place delivery rules are defined (DEC-004).
 *
 * | Sticker | Newspaper | Leaflets |
 * |---------|-----------|----------|
 * | NONE    | yes       | yes      |
 * | JA      | yes       | yes      |
 * | NEE_JA  | yes       | no       |
 * | NEE_NEE | no        | no       |
 */
fun deliveryFor(sticker: Sticker): Delivery = when (sticker) {
    Sticker.NONE, Sticker.JA -> Delivery.BOTH
    Sticker.NEE_JA -> Delivery.NEWSPAPER_ONLY
    Sticker.NEE_NEE -> Delivery.NOTHING
}

/**
 * What [address] receives in a round with [round] contents: nothing if the address does not exist;
 * otherwise the sticker result, minus items blocked by per-address exceptions (ADR-09), minus items
 * not in this round.
 */
fun deliveryFor(address: Address, round: RoundContents): Delivery {
    if (!address.exists) return Delivery.NOTHING
    val bySticker = deliveryFor(address.sticker)
    return Delivery(
        newspaper = bySticker.newspaper && !address.exceptionNoNewspaper && round.newspaper,
        leaflets = bySticker.leaflets && !address.exceptionNoLeaflets && round.leaflets,
    )
}

/** A round with both items: what the editor shows next to each address (STK-03). */
val FullRound = RoundContents(newspaper = true, leaflets = true)

/** The state shown for [address] in a round with [round] contents (default: the editor's full round). */
fun deliveryKind(address: Address, round: RoundContents = FullRound): DeliveryKind {
    if (!address.exists) return DeliveryKind.DOES_NOT_EXIST
    val delivery = deliveryFor(address, round)
    return when {
        delivery.newspaper && delivery.leaflets -> DeliveryKind.BOTH
        delivery.newspaper -> DeliveryKind.NEWSPAPER_ONLY
        delivery.leaflets -> DeliveryKind.LEAFLETS_ONLY
        else -> DeliveryKind.NOTHING
    }
}
