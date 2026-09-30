package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Delivery
import nl.ericmulder.krantenwijk.domain.model.DeliveryKind
import nl.ericmulder.krantenwijk.domain.model.RoundContents
import nl.ericmulder.krantenwijk.domain.model.Sticker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class DeliveryRulesTest {

    private val both = RoundContents(newspaper = true, leaflets = true)
    private val newspaperOnly = RoundContents(newspaper = true, leaflets = false)
    private val leafletsOnly = RoundContents(newspaper = false, leaflets = true)

    @Nested
    inner class StickerTable {
        // Plan §3.3, one row per sticker.
        @ParameterizedTest(name = "{0} → newspaper={1}, leaflets={2}")
        @CsvSource(
            "NONE,    true,  true",
            "JA,      true,  true",
            "NEE_JA,  true,  false",
            "NEE_NEE, false, false",
        )
        fun `sticker determines delivery`(sticker: Sticker, newspaper: Boolean, leaflets: Boolean) {
            assertEquals(Delivery(newspaper, leaflets), deliveryFor(sticker))
        }
    }

    @Nested
    inner class PerAddress {
        @ParameterizedTest
        @EnumSource(Sticker::class)
        fun `non-existing address never receives anything`(sticker: Sticker) {
            val address = Address(houseNumber = 10, exists = false, sticker = sticker)
            assertEquals(Delivery.NOTHING, deliveryFor(address, both))
        }

        @ParameterizedTest
        @EnumSource(Sticker::class)
        fun `full round follows the sticker table`(sticker: Sticker) {
            assertEquals(deliveryFor(sticker), deliveryFor(Address(houseNumber = 2, sticker = sticker), both))
        }

        @ParameterizedTest
        @EnumSource(Sticker::class)
        fun `round without leaflets gives no leaflets anywhere`(sticker: Sticker) {
            val delivery = deliveryFor(Address(houseNumber = 2, sticker = sticker), newspaperOnly)
            assertEquals(false, delivery.leaflets)
            assertEquals(deliveryFor(sticker).newspaper, delivery.newspaper)
        }

        @ParameterizedTest
        @EnumSource(Sticker::class)
        fun `round without newspaper gives no newspaper anywhere`(sticker: Sticker) {
            val delivery = deliveryFor(Address(houseNumber = 2, sticker = sticker), leafletsOnly)
            assertEquals(false, delivery.newspaper)
            assertEquals(deliveryFor(sticker).leaflets, delivery.leaflets)
        }

        @Test
        fun `exception blocks newspaper despite sticker`() {
            val address = Address(houseNumber = 4, sticker = Sticker.JA, exceptionNoNewspaper = true)
            assertEquals(Delivery(newspaper = false, leaflets = true), deliveryFor(address, both))
        }

        @Test
        fun `exception blocks leaflets despite sticker`() {
            val address = Address(houseNumber = 4, sticker = Sticker.NONE, exceptionNoLeaflets = true)
            assertEquals(Delivery.NEWSPAPER_ONLY, deliveryFor(address, both))
        }

        @Test
        fun `exceptions never add items the sticker blocks`() {
            val address = Address(houseNumber = 4, sticker = Sticker.NEE_NEE, exceptionNoLeaflets = true)
            assertEquals(Delivery.NOTHING, deliveryFor(address, both))
        }
    }

    @Nested
    inner class Kind {
        @ParameterizedTest(name = "{0} → {1}")
        @CsvSource("NONE, BOTH", "JA, BOTH", "NEE_JA, NEWSPAPER_ONLY", "NEE_NEE, NOTHING")
        fun `editor shows the full-round result`(sticker: Sticker, kind: DeliveryKind) {
            assertEquals(kind, deliveryKind(Address(houseNumber = 2, sticker = sticker)))
        }

        @ParameterizedTest
        @EnumSource(Sticker::class)
        fun `does not exist wins over any sticker`(sticker: Sticker) {
            assertEquals(DeliveryKind.DOES_NOT_EXIST, deliveryKind(Address(houseNumber = 2, exists = false, sticker = sticker)))
        }

        @Test
        fun `leaflets only via exception or a leaflets-only round`() {
            assertEquals(DeliveryKind.LEAFLETS_ONLY, deliveryKind(Address(houseNumber = 2, exceptionNoNewspaper = true)))
            assertEquals(DeliveryKind.LEAFLETS_ONLY, deliveryKind(Address(houseNumber = 2), leafletsOnly))
            assertEquals(DeliveryKind.NOTHING, deliveryKind(Address(houseNumber = 2, sticker = Sticker.NEE_JA), leafletsOnly))
        }
    }

    @Test
    fun `round must include at least one item`() {
        assertThrows<IllegalArgumentException> { RoundContents(newspaper = false, leaflets = false) }
    }
}
