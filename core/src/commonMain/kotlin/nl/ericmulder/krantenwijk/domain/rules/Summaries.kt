package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.RoundContents
import nl.ericmulder.krantenwijk.domain.model.Sticker

/** Sticker counts over existing addresses (BLD-02, STK-04). */
data class StickerCounts(val none: Int, val ja: Int, val neeJa: Int, val neeNee: Int) {
    val total: Int get() = none + ja + neeJa + neeNee
}

/** Delivery counts for a round: items to take along and addresses that receive something (RND-02, BLD-06). */
data class DeliveryCounts(val newspapers: Int, val leaflets: Int, val deliverableAddresses: Int)

/** Counts stickers of [addresses], skipping those marked "does not exist". Counts apartments, not buildings. */
fun stickerSummary(addresses: List<Address>): StickerCounts {
    val bySticker = addresses.filter { it.exists }.groupingBy { it.sticker }.eachCount()
    return StickerCounts(
        none = bySticker[Sticker.NONE] ?: 0,
        ja = bySticker[Sticker.JA] ?: 0,
        neeJa = bySticker[Sticker.NEE_JA] ?: 0,
        neeNee = bySticker[Sticker.NEE_NEE] ?: 0,
    )
}

/** Counts what [addresses] receive in a round with [round] contents. */
fun deliverySummary(addresses: List<Address>, round: RoundContents): DeliveryCounts {
    val deliveries = addresses.map { deliveryFor(it, round) }
    return DeliveryCounts(
        newspapers = deliveries.count { it.newspaper },
        leaflets = deliveries.count { it.leaflets },
        deliverableAddresses = deliveries.count { !it.isNothing },
    )
}
