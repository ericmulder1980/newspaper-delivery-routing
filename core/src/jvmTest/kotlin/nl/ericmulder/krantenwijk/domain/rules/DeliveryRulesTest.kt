package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Delivery
import nl.ericmulder.krantenwijk.domain.model.DeliveryKind
import nl.ericmulder.krantenwijk.domain.model.RoundContents
import nl.ericmulder.krantenwijk.domain.model.Sticker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class DeliveryRulesTest {

    private val both = RoundContents(leaflets = true)
    private val newspaperOnly = RoundContents(leaflets = false)

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

        @Test
        fun `every round includes the newspaper`() {
            assertEquals(true, both.newspaper)
            assertEquals(true, newspaperOnly.newspaper)
        }

        @ParameterizedTest
        @EnumSource(Sticker::class)
        fun `no newspaper exception means nothing at all (DEC-017)`(sticker: Sticker) {
            val address = Address(houseNumber = 4, sticker = sticker, exceptionNoNewspaper = true)
            assertEquals(Delivery.NOTHING, deliveryFor(address, both))
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
        fun `leaflets are never delivered without the newspaper (DEC-017)`() {
            val combos = Sticker.entries.flatMap { sticker ->
                listOf(true, false).flatMap { exists ->
                    listOf(true, false).flatMap { noNewspaper ->
                        listOf(true, false).flatMap { noLeaflets ->
                            listOf(both, newspaperOnly).map { round ->
                                deliveryFor(
                                    Address(2, exists = exists, sticker = sticker, exceptionNoNewspaper = noNewspaper, exceptionNoLeaflets = noLeaflets),
                                    round,
                                )
                            }
                        }
                    }
                }
            }
            assertEquals(emptyList<Delivery>(), combos.filter { it.leaflets && !it.newspaper })
        }
    }
}
