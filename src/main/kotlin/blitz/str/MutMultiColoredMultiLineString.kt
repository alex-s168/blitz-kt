package blitz.str

import blitz.term.AnsiiMode
import blitz.term.Terminal

class MutMultiColoredMultiLineString(
     var fill: ColoredChar
) {
    val lines = mutableListOf<MutMultiColoredString>()

    // TODO: wrap at \n

    override fun equals(other: Any?): Boolean =
        lines == other

    override fun hashCode(): Int =
        lines.hashCode()

    /** if out of bounds, extends with @see fill */
    operator fun get(row: Int, col: Int): ColoredChar {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutMultiColoredString(fill = fill))
            }
        }
        return lines[row][col]
    }

    /** if out of bounds, extends with @see fill */
    operator fun get(row: Int): MutMultiColoredString {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutMultiColoredString(fill = fill))
            }
        }
        return lines[row]
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(row: Int, col: Int, value: ColoredChar) {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutMultiColoredString(fill = fill))
            }
        } else {
            lines[row].fill = fill
        }
        lines[row][col] = value
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(row: Int, col: Int, value: Char) =
        set(row, col, ColoredChar(value))

    /** if out of bounds, extends with @see fill */
    operator fun set(row: Int, colStart: Int, value: CharSequence) {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutMultiColoredString(fill = fill))
            }
        } else {
            lines[row].fill = fill
        }
        lines[row][colStart] = value
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(row: Int, colStart: Int, value: MutMultiColoredString) {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutMultiColoredString(fill = fill))
            }
        } else {
            lines[row].fill = fill
        }
        lines[row][colStart] = value
    }

    /** if out of bounds, extends with @see fill */
    fun set(row: Int, colStart: Int, va: Char, style: AnsiiMode) =
        set(row, colStart, ColoredChar(va, style))

    /** if out of bounds, extends with @see fill */
    fun set(row: Int, colStart: Int, va: String, style: AnsiiMode) =
        set(row, colStart, MutMultiColoredString.from(va, style))

    /** if out of bounds, extends with @see fill */
    operator fun set(rowStart: Int, colStart: Int, value: MutMultiLineString) {
        value.lines.forEachIndexed { index, line ->
            this[index + rowStart, colStart] = line
        }
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(rowStart: Int, colStart: Int, value: MutMultiLineString, style: AnsiiMode) {
        value.lines.forEachIndexed { index, line ->
            this.set(index + rowStart, colStart, line.toString(), style)
        }
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(rowStart: Int, colStart: Int, value: MutMultiColoredMultiLineString) {
        value.lines.forEachIndexed { index, line ->
            this[index + rowStart, colStart] = line
        }
    }

    fun box(start: Pair<Int, Int>, end: Pair<Int, Int>, set: BoxDrawingCharSet, style: AnsiiMode = AnsiiMode(mutableListOf())) {
        set.draw(start, end) { x, y, c -> this[y, x] = ColoredChar(c, style) }
    }

    override fun toString(): String =
        lines.joinToString(separator = "\n")
}