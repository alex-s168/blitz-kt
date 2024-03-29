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

    fun transposeCopy(): Matrix<T> =
        Matrix(width, height) { x, y -> this[y, x] }

    fun perfectSquareCopy(): Matrix<T> =
        min(width, height).let { wh ->
            Matrix(wh, wh) { x, y -> this[x, y] }
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

    // TODO: make better
    override fun toString(): String =
        "--\n" +
        rows.joinToString(separator = "\n") {
            it.joinToString(separator = ", ", prefix = "| ", postfix = " |")
        } + "\n--"
}