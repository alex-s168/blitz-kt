package blitz.collections

import blitz.toBit2
import blitz.toBool
import blitz.toByte
import kotlin.math.ceil

// TODO: make it hybrid to a real bitset if a lot of elements

class BitVec private constructor(
    private val byteVec: ByteVec
): Vec<Boolean> {
    constructor(initCap: Int = 0): this(ByteVec(initCap))

    override val size: Int
        get() = byteVec.size

    // TODO: implement better
    fun toBytes(): ByteArray =
        toString()
            .padEnd(ceil(byteVec.size.toFloat() / 8).toInt(), '0')
            .chunked(8)
            .map { it.toByte(2) }
            .toByteArray()

    override fun flip() =
        byteVec.flip()

    override fun copy(): Vec<Boolean> =
        BitVec(byteVec)

    override fun reserve(amount: Int) =
        byteVec.reserve(amount)

    override fun popBack(): Boolean =
        byteVec.popBack().toBool()

    override fun pushBack(elem: Boolean) =
        byteVec.pushBack(elem.toByte())

    override fun get(index: Int): Boolean =
        byteVec[index].toBool()

    override fun iterator(): Iterator<Boolean> =
        byteVec.iterator().mapModifier { it.toBool() }

    override fun toString(): String =
        joinToString(separator = "") { it.toBit2().toString() }

    override fun set(index: Int, value: Boolean) {
        byteVec[index] = value.toByte()
    }

    companion object {
        // TODO: implement better
        fun from(bytes: ByteArray): BitVec =
            BitVec(ByteVec.from(
                bytes.asSequence()
                    .map { it
                        .toString(2)
                        .map { c -> (c == '1').toByte() }
                    }
                    .flatten()
            ))
    }
}