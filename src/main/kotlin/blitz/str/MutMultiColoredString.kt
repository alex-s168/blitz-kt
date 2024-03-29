package blitz.str

import blitz.collections.mergeNeighbors
import blitz.term.AnsiiMode
import blitz.term.Terminal

class MutMultiColoredString(var fill: ColoredChar) {
    val chars = mutableListOf<ColoredChar>()

    val length
        get() = chars.size

    override fun equals(other: Any?): Boolean =
        chars == other

    override fun hashCode(): Int =
        chars.hashCode()

    override fun toString(): String {
        val byColors = chars.mergeNeighbors { it.style }
        val res = MutString(fill = ' ')
        byColors.forEach {
            val style = it.first
            val str = it.second.convToString()
            res.append(Terminal.encodeString(str, style))
        }
        return res.toString()
    }

    fun add(str: String, style: AnsiiMode = AnsiiMode(mutableListOf())) {
        str.mapTo(chars) { ColoredChar(it, style) }
    }

    fun add(str: Iterable<ColoredChar>) {
        chars.addAll(str)
    }

    fun add(ch: ColoredChar) {
        chars.add(ch)
    }

    operator fun get(index: Int): ColoredChar {
        if (index >= length) {
            repeat(index - length + 1) {
                chars.add(fill)
            }
        }
        return chars[index]
    }

    operator fun set(index: Int, value: ColoredChar) {
        if (index >= length) {
            repeat(index - length + 1) {
                chars.add(fill)
            }
        }
        chars[index] = value
    }

    fun set(start: Int, str: CharSequence, style: AnsiiMode) {
        if (start + str.length >= length) {
            repeat(start + str.length - length + 1) {
                chars.add(fill)
            }
        }
        str.forEachIndexed { index, c ->
            chars[start + index] = ColoredChar(c, style)
        }
    }

    operator fun set(start: Int, str: CharSequence) {
        set(start, str, AnsiiMode(mutableListOf()))
    }

    operator fun set(start: Int, str: MutMultiColoredString) {
        if (start + str.length >= length) {
            repeat(start + str.length - length + 1) {
                chars.add(fill)
            }
        }
        str.chars.forEachIndexed { index, c ->
            chars[start + index] = c
        }
    }

    companion object {
        fun from(str: String, style: AnsiiMode = AnsiiMode(mutableListOf())) =
            MutMultiColoredString(fill = ColoredChar(' ')).also {
                it.add(str, style)
            }
    }
}