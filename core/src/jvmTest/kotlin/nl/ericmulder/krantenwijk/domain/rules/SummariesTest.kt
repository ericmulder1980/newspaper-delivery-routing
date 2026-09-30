package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.RoundContents
import nl.ericmulder.krantenwijk.domain.model.Sticker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SummariesTest {

    // Building 12, apartments A–F; 12F does not exist.
    private val apartments = listOf(
        Address(12, "A", sticker = Sticker.NONE),
        Address(12, "B", sticker = Sticker.JA),
        Address(12, "C", sticker = Sticker.NEE_JA),
        Address(12, "D", sticker = Sticker.NEE_JA),
        Address(12, "E", sticker = Sticker.NEE_NEE),
        Address(12, "F", sticker = Sticker.NEE_NEE, exists = false),
    )

    @Test
    fun `sticker summary excludes non-existing apartments`() {
        assertEquals(StickerCounts(none = 1, ja = 1, neeJa = 2, neeNee = 1), stickerSummary(apartments))
        assertEquals(5, stickerSummary(apartments).total)
    }

    @Test
    fun `delivery summary for a full round`() {
        val counts = deliverySummary(apartments, RoundContents(leaflets = true))
        assertEquals(DeliveryCounts(newspapers = 4, leaflets = 2, deliverableAddresses = 4), counts)
    }

    @Test
    fun `delivery summary for a round without leaflets`() {
        val counts = deliverySummary(apartments, RoundContents(leaflets = false))
        assertEquals(DeliveryCounts(newspapers = 4, leaflets = 0, deliverableAddresses = 4), counts)
    }

    @Test
    fun `delivery summary respects exceptions`() {
        val withExceptions = apartments +
            Address(12, "G", exceptionNoNewspaper = true) +
            Address(12, "H", exceptionNoLeaflets = true)
        val counts = deliverySummary(withExceptions, RoundContents(leaflets = true))
        assertEquals(DeliveryCounts(newspapers = 5, leaflets = 2, deliverableAddresses = 5), counts)
    }

    @Test
    fun `empty building has zero counts`() {
        assertEquals(StickerCounts(0, 0, 0, 0), stickerSummary(emptyList()))
        assertEquals(DeliveryCounts(0, 0, 0), deliverySummary(emptyList(), RoundContents(leaflets = false)))
    }
}
