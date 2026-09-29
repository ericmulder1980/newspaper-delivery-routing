package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OrderingTest {

    private fun labels(addresses: List<Address>) = addresses.map { "${it.houseNumber}${it.addition ?: ""}" }

    private val mixed = listOf(
        Address(13), Address(12, "B"), Address(12), Address(10), Address(12, "A"), Address(14, "a"),
    )

    @Test
    fun `sorts by number then addition`() {
        assertEquals(listOf("10", "12", "12A", "12B", "13", "14a"), labels(inWalkingOrder(mixed, Direction.ASCENDING)))
    }

    @Test
    fun `descending reverses the walking order`() {
        assertEquals(listOf("14a", "13", "12B", "12A", "12", "10"), labels(inWalkingOrder(mixed, Direction.DESCENDING)))
    }

    @Test
    fun `numeric suffixes sort naturally`() {
        val sorted = listOf("10", "2", "1", "11", "20", "3").sortedWith(NaturalOrder)
        assertEquals(listOf("1", "2", "3", "10", "11", "20"), sorted)
    }

    @Test
    fun `letters sort case-insensitively`() {
        assertEquals(listOf("a", "B", "c"), listOf("c", "a", "B").sortedWith(NaturalOrder))
    }

    @Test
    fun `no addition comes first`() {
        assertEquals(listOf(null, "A", "bis"), listOf("bis", null, "A").sortedWith(NaturalOrder))
    }

    @Test
    fun `numbers sort before letters`() {
        assertEquals(listOf("2", "A"), listOf("A", "2").sortedWith(NaturalOrder))
    }

    @Test
    fun `mixed additions compare token by token`() {
        val sorted = listOf("2-10", "2-2", "hs", "1", "bis").sortedWith(NaturalOrder)
        assertEquals(listOf("1", "2-2", "2-10", "bis", "hs"), sorted)
    }

    @Test
    fun `very long digit runs do not overflow`() {
        assertTrue(NaturalOrder.compare("99999999999999999999", "100000000000000000000") < 0)
        assertTrue(NaturalOrder.compare("007", "7") != 0) // distinct strings stay distinct
    }

    @Test
    fun `case variants are ordered consistently in both directions`() {
        assertEquals(0, NaturalOrder.compare("A", "A"))
        assertTrue(NaturalOrder.compare("A", "a") != 0)
        assertEquals(Integer.signum(NaturalOrder.compare("A", "a")), -Integer.signum(NaturalOrder.compare("a", "A")))
    }
}
