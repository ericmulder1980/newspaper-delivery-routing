package nl.ericmulder.krantenwijk.domain.rules

import nl.ericmulder.krantenwijk.domain.model.Address
import nl.ericmulder.krantenwijk.domain.model.Direction

/**
 * Natural order for house number additions and apartment suffixes: numbers compare by value and
 * letters case-insensitively, so "2" < "10" and "A" < "b". No addition (null) comes first.
 * Separators such as spaces and hyphens are ignored ("12-2" and "12 2" sort alike).
 */
val NaturalOrder: Comparator<String?> = Comparator { a, b ->
    when {
        a == b -> 0
        a == null -> -1
        b == null -> 1
        else -> compareTokens(tokenize(a), tokenize(b)).takeIf { it != 0 } ?: a.compareTo(b)
    }
}

/** Order of addresses within a street section by number, then addition: 12, 12A, 12B, 13. */
val AddressNumberOrder: Comparator<Address> =
    compareBy<Address> { it.houseNumber }.then { a, b -> NaturalOrder.compare(a.addition, b.addition) }

/** [addresses] in the order they are walked past (ADR-06). */
fun inWalkingOrder(addresses: List<Address>, direction: Direction): List<Address> = when (direction) {
    Direction.ASCENDING -> addresses.sortedWith(AddressNumberOrder)
    Direction.DESCENDING -> addresses.sortedWith(AddressNumberOrder.reversed())
}

private val TOKEN = Regex("""\d+|\p{L}+""")

private fun tokenize(value: String): List<String> = TOKEN.findAll(value).map { it.value }.toList()

private fun compareTokens(a: List<String>, b: List<String>): Int {
    for (i in 0 until minOf(a.size, b.size)) {
        val result = compareToken(a[i], b[i])
        if (result != 0) return result
    }
    return a.size.compareTo(b.size)
}

private fun compareToken(a: String, b: String): Int {
    val aIsNumber = a[0].isDigit()
    val bIsNumber = b[0].isDigit()
    return when {
        aIsNumber && bIsNumber -> compareNumeric(a, b)
        aIsNumber -> -1
        bIsNumber -> 1
        else -> a.compareTo(b, ignoreCase = true)
    }
}

/** Compares digit strings by value without overflow: fewer significant digits is smaller. */
private fun compareNumeric(a: String, b: String): Int {
    val x = a.trimStart('0')
    val y = b.trimStart('0')
    return if (x.length != y.length) x.length.compareTo(y.length) else x.compareTo(y)
}
