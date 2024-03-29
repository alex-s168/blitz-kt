package blitz.str

import blitz.term.AnsiiMode
import blitz.term.Terminal

class ColoredChar(
    val char: Char,
    val style: AnsiiMode = AnsiiMode(mutableListOf())
) {
    override fun equals(other: Any?): Boolean =
        char == other

    override fun hashCode(): Int =
        char.hashCode()

    override fun toString(): String =
        Terminal.encodeString("$char", style)
}

fun Iterable<ColoredChar>.convToString(): String =
    joinToString(separator = "")