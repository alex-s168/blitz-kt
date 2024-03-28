package blitz.str

class MutMultiLineString(
    var fill: Char
) {
    val lines = mutableListOf<MutString>()

    // TODO: wrap at \n

    /** if out of bounds, extends with @see fill */
    operator fun get(row: Int, col: Int): Char {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutString(fill = fill))
            }
        }
        return lines[row][col]
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(row: Int, col: Int, value: Char) {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutString(fill = fill))
            }
        } else {
            lines[row].fill = fill
        }
        lines[row][col] = value
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(row: Int, colStart: Int, value: CharSequence) {
        if (row >= lines.size) {
            repeat(row - lines.size + 1) {
                lines.add(MutString(fill = fill))
            }
        } else {
            lines[row].fill = fill
        }
        lines[row][colStart] = value
    }

    /** if out of bounds, extends with @see fill */
    operator fun set(rowStart: Int, colStart: Int, value: MutMultiLineString) {
        value.lines.forEachIndexed { index, line ->
            this[index + rowStart, colStart] = line
        }
    }

    override fun toString(): String =
        lines.joinToString(separator = "\n")
}