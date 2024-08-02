package blitz.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class Dense16x16BoolMap(
    val packed: UShortArray = UShortArray(16)
) {
    fun fillRowsWith(value: UShort) {
        repeat(16) {
            packed[it] = value
        }
    }

    fun clear() {
        fillRowsWith(0u)
    }

    fun getPackedBytes(dest: ByteArray, destOff: Int = 0) {
        require(dest.size - destOff >= 32)
        packed.forEachIndexed { index, uShort ->
            val didx = destOff + index * 2
            dest[didx] = (uShort and 0xFFu).toByte()
            dest[didx + 1] = ((uShort.toInt() ushr 8) and 0xFF).toByte()
        }
    }

    fun fromPackedBytes(src: ByteArray, srcOff: Int = 0) {
        require(src.size - srcOff >= 32)

        repeat(16) { uShortIdx ->
            val sidx = srcOff + uShortIdx * 2
            val uShort = ((src[sidx].toInt() shl 8) or (src[sidx].toInt())).toUShort()
            packed[uShortIdx] = uShort
        }
    }

    fun appendFrom(other: Dense16x16BoolMap) {
        other.packed.forEachIndexed { index, otherUShort ->
            val old = packed[index]
            packed[index] = old or otherUShort
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun forEachSetRow(fn: (Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        repeat(16) {
            if (anyInRow(it)) {
                fn(it)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun forEachSet(fn: (Int, Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        forEachSetRow { rowId ->
            val row = packed[rowId]
            val lo = row and 0xFFu
            val hi = (row.toInt() shr 8) and 0xFF

            if (lo > 0u) {
                var acc = lo.toInt()
                repeat(8) {
                    val v = acc and 1
                    acc = acc shr 1

                    if (v > 0) fn(rowId, it)
                }
            }

            if (hi > 0) {
                var acc = hi
                repeat(8) {
                    val v = acc and 1
                    acc = acc shr 1

                    if (v > 0) fn(rowId, 8 + it)
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun <T> getSetPosList(dest: MutableList<T> = mutableListOf(), crossinline mapfn: (Int, Int) -> T): MutableList<T> {
        contract {
            callsInPlace(mapfn)
        }

        forEachSet { x, y ->
            dest.add(mapfn(x, y))
        }

        return dest
    }

    fun packedSetPosList(dest: ByteVec = ByteVec(16)): ByteVec {
        forEachSet { x, y ->
            val packed = packPos(x, y)
            dest.pushBack(packed.toByte())
        }
        return dest
    }

    companion object {
        fun packPos(row: Int, col: Int): UByte =
            ((row shl 4) or col).toUByte()

        @OptIn(ExperimentalContracts::class)
        inline fun <T> unpackPos(packed: UByte, fn: (Int, Int) -> T): T {
            contract {
                callsInPlace(fn)
            }

            return fn((packed.toInt() shr 4) and 0xF, packed.toInt() and 0xF)
        }

        inline fun <T> unpackPos(packed: Byte, fn: (Int, Int) -> T): T =
            unpackPos(packed.toUByte(), fn)

        inline fun forEachPackedPos(packed: Iterator<Byte>, fn: (Int, Int) -> Unit) {
            for (it in packed) {
                unpackPos(it, fn)
            }
        }

        inline fun forEachPackedPos(packed: Iterable<Byte>, fn: (Int, Int) -> Unit) =
            forEachPackedPos(packed.iterator(), fn)

        inline fun forEachPackedPos(packed: Sequence<Byte>, fn: (Int, Int) -> Unit) =
            forEachPackedPos(packed.iterator(), fn)

        inline fun consumeAllPackedPos(vec: ByteVec, batching: ByteArray, fn: (Int, Int) -> Unit) {
            vec.consumePopBack(batching) { it ->
                unpackPos(it, fn)
            }
        }
    }

    fun anyInRow(row: Int) =
        packed[row] > 0u

    fun setRow(row: Int, value: UShort) {
        packed[row] = value
    }

    fun clearRow(row: Int) {
        setRow(row, 0u)
    }

    fun countSetInRow(row: Int) =
        packed[row].countOneBits()

    fun columnMask(col: Int) =
        1 shl col

    operator fun get(row: Int, col: Int) =
        (packed[row].toInt() and columnMask(col)) > 0

    operator fun get(packedRowCol: UByte) =
        unpackPos(packedRowCol) { x, y -> get(x, y) }

    operator fun get(packedRowCol: Byte) =
        unpackPos(packedRowCol) { x, y -> get(x, y) }

    fun set(row: Int, col: Int) {
        packed[row] = (packed[row].toInt() or columnMask(col)).toUShort()
    }

    fun unset(row: Int, col: Int) {
        val mask = columnMask(col).inv()
        packed[row] = (packed[row].toInt() and mask).toUShort()
    }

    operator fun set(row: Int, col: Int, value: Boolean) {
        if (value) {
            set(row, col)
        } else {
            unset(row, col)
        }
    }

    operator fun set(packedRowCol: Byte, value: Boolean) =
        unpackPos(packedRowCol) { x, y -> set(x, y, value) }

    operator fun set(packedRowCol: UByte, value: Boolean) =
        unpackPos(packedRowCol) { x, y -> set(x, y, value) }
}