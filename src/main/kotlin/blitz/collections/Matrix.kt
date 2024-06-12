package blitz.collections

import blitz.str.MutMultiLineString
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

    operator fun get(pos: Pair<Int, Int>): T =
        rows[pos.second][pos.first]

    operator fun set(pos: Pair<Int, Int>, value: T) {
        rows[pos.second][pos.first] = value
    }

    fun row(row: Int): IndexableSequence<T> =
        rows[row].asSequence().asIndexable()

    fun column(col: Int): IndexableSequence<T> =
        generateSequenceWithIndex(height) { row -> this[col, row] }

    fun elements(): Sequence<T> =
        rows.asSequence().flatten()

    fun elementsWithIndexes(): Sequence<Pair<T, Pair<Int, Int>>> =
        rows.asSequence()
            .flatMapIndexed { row, ts ->
                ts.mapIndexed { col, t ->
                    t to (col to row)
                }
            }

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
        Matrix(height, width) { x, y -> this[y, x] }

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

    fun randomPos(): Pair<Int, Int> =
        (0..<width).random() to (0..<height).random()

    fun toString(alignRight: Boolean): String {
        val str = stringMat()

        val widths = str.columnWidths()
        val heights = str.rowHeights()

        val out = MutMultiLineString(' ')

        var y = 0
        str.rows.forEachIndexed { row, elems ->
            var x = 0
            elems.forEachIndexed { col, elem ->
                if (alignRight)
                    out[y, x + widths[col] - elem.lines().maxOf { it.length }] = MutMultiLineString.from(elem, ' ')
                else
                    out[y, x] = elem
                x += widths[col] + 1
            }
            y += heights[row] + 1
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
    IntArray(width) { col -> column(col).maxOf { it.lines().maxOf { it.length } } }

fun Matrix<String>.rowHeights(): IntArray =
    IntArray(height) { row -> row(row).maxOf { it.lines().size } }

fun Matrix<String>.lengths(): Matrix<Int> =
    Matrix(width, height) { x, y -> this[x, y].length }

operator fun Matrix<Float>.plusAssign(other: Matrix<Float>) {
    fill { x, y -> this[x, y] + other[x, y] }
}