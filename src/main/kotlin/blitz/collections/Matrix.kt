package blitz.collections

import kotlin.math.min

class Matrix<T>(
    val width: Int,
    val height: Int,
    private val init: (x: Int, y: Int) -> T
) {
    val rows = MutableList(height) { y ->
        MutableList(width) { x ->
            init(x, y)
        }
    }

    operator fun get(x: Int, y: Int): T =
        rows[y][x]

    operator fun set(x: Int, y: Int, value: T) {
        rows[y][x] = value
    }

    fun column(col: Int): IndexableSequence<T> =
        generateSequenceWithIndex(height) { row -> this[col, row] }

    fun fill(fn: (x: Int, y: Int) -> T) {
        repeat(height) { row ->
            repeat(width) { col ->
                this[col, row] = fn(col, row)
            }
        }
    }

    fun transpose(dest: Matrix<T>) {
        dest.fill { x, y -> this[y, x] }
    }

    fun transposeCopy(): Matrix<T> =
        Matrix(width, height) { x, y -> this[y, x] }

    fun perfectSquareCopy(): Matrix<T> =
        min(width, height).let { wh ->
            Matrix(wh, wh) { x, y -> this[x, y] }
        }

    fun copyTo(dest: Matrix<T>) {
        dest.fill { x, y -> this[x, y] }
    }

    fun copy(): Matrix<T> =
        Matrix(width, height) { x, y -> this[x, y] }

    fun diagonalTopLeftToBottomRight(to: MutableList<T> = mutableListOf()): MutableList<T> {
        repeat(min(width, height)) { pos ->
            to.add(this[pos, pos])
        }
        return to
    }

    fun diagonalTopRightToBottomLeft(to: MutableList<T> = mutableListOf()): MutableList<T> {
        val wh = min(width, height)
        for(pos in wh - 1 downTo 0) {
            to.add(this[pos, wh - 1 - pos])
        }
        return to
    }

    fun diagonals(to: MutableList<MutableList<T>> = mutableListOf()): MutableList<MutableList<T>> {
        to.add(diagonalTopLeftToBottomRight())
        to.add(diagonalTopRightToBottomLeft())
        return to
    }

    fun stringMat(): Matrix<String> =
        Matrix(width, height) { x, y -> this[x, y].toString() }

    fun stringMatTo(dest: Matrix<String>) {
        dest.fill { x, y -> this[x, y].toString() }
    }

    fun window(w: Int, h: Int): Matrix<Matrix<T>> =
        Matrix(width / w, height / h) { x, y ->
            Matrix(w, h) { sx, sy -> this[x * w + sx, y * w + sy] }
        }

    fun toString(alignRight: Boolean): String {
        val str = stringMat()
        val widths = str.columnWidths()
        val out = StringBuilder()

        val padfn = if (alignRight) String::padStart else String::padEnd

        str.rows.forEachIndexed { row, elems ->
            if (row > 0)
                out.append('\n')
            elems.forEachIndexed { col, x ->
                if (col > 0)
                    out.append(' ')
                out.append(padfn(x, widths[col], ' '))
            }
        }

        return out.toString()
    }

    override fun toString(): String =
        toString(true)

    override fun equals(other: Any?): Boolean =
        rows == other

    override fun hashCode(): Int =
        rows.hashCode()
}

fun Matrix<String>.columnWidths(): IntArray =
    IntArray(width) { col -> column(col).maxOf { it.length } }

fun Matrix<String>.lengths(): Matrix<Int> =
    Matrix(width, height) { x, y -> this[x, y].length }