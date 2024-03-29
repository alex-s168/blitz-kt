package blitz.str

import java.util.stream.IntStream

class MutString(
    init: String = "",
    var fill: Char
): CharSequence, Appendable {
    private val builder = StringBuilder(init)

    override fun chars(): IntStream =
        builder.chars()

    override fun codePoints(): IntStream =
        builder.codePoints()

    override val length: Int
        get() = builder.length

    override operator fun get(index: Int): Char {
        if (index >= length) {
            repeat(index - length + 1) {
                builder.append(fill)
            }
        }
        return builder[index]
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(index: Int, value: Char) {
        if (index >= length) {
            repeat(index - length + 1) {
                builder.append(fill)
            }
        }
        builder[index] = value
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(start: Int, str: CharSequence) {
        if (start + str.length >= length) {
            repeat(start + str.length - length + 1) {
                builder.append(fill)
            }
        }
        builder.replace(start, start + str.length, str.toString())
    }

    override fun toString(): String =
        builder.toString()

    override fun append(csq: CharSequence?): Appendable {
        builder.append(csq)
        return this
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
        builder.append(csq, start, end)
        return this
    }

    override fun append(c: Char): Appendable {
        builder.append(c)
        return this
    }

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        builder.subSequence(startIndex, endIndex)
}