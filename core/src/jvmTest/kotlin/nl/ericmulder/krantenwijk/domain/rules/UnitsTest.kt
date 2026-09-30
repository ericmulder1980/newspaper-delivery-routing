package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Building
import nl.ericmulder.krantenwijk.domain.model.SuffixType
import nl.ericmulder.krantenwijk.domain.model.unitLabel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UnitsTest {

    @Test
    fun `A to L gives 12 letters`() {
        assertEquals(
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"),
            generateUnits("A", "L", SuffixType.LETTER),
        )
    }

    @Test
    fun `A to Z gives 26 letters`() {
        assertEquals(26, generateUnits("A", "Z", SuffixType.LETTER).size)
    }

    @Test
    fun `lower-case letters are accepted and upper-cased`() {
        assertEquals(listOf("A", "B", "C"), generateUnits("a", " c ", SuffixType.LETTER))
    }

    @Test
    fun `1 to 20 gives 20 numeric units`() {
        assertEquals((1..20).map(Int::toString), generateUnits("1", "20", SuffixType.NUMBER))
    }

    @ParameterizedTest(name = "{0}–{1} {2} is rejected")
    @CsvSource(
        "L, A, LETTER",
        "AB, C, LETTER",
        "1, 3, LETTER",
        "Ä, B, LETTER",
        "20, 1, NUMBER",
        "0, 5, NUMBER",
        "A, 5, NUMBER",
    )
    fun `invalid unit ranges are rejected`(from: String, to: String, type: SuffixType) {
        assertThrows<IllegalArgumentException> { generateUnits(from, to, type) }
    }

    @Test
    fun `preview helper returns null for invalid input`() {
        assertEquals(listOf("A", "B"), unitsOrNull("A", "B", SuffixType.LETTER))
        assertEquals(null, unitsOrNull("B", "A", SuffixType.LETTER))
        assertEquals(null, unitsOrNull("", "5", SuffixType.NUMBER))
    }

    @Test
    fun `labels use the separator for the suffix type`() {
        assertEquals("", separatorFor(SuffixType.LETTER))
        assertEquals("-", separatorFor(SuffixType.NUMBER))
        val building = Building(1, 12, SuffixType.NUMBER, "-")
        assertEquals("12-3", building.unitLabel("3"))
    }
}
