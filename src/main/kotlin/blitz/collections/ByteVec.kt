package blitz.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ByteVec(private val initCap: Int = 0): Vec<Byte>, ByteBatchSequence {
    override var size = 0
    private var cap = initCap
    private var array = ByteArray(initCap)

    override fun clear() {
        size = 0
        if (array.size <= initCap) {
            cap = array.size
        } else {
            cap = initCap
            array = ByteArray(initCap)
        }
    }

    fun copyAsArray(): ByteArray =
        array.copyOfRange(0, size)

    fun copyIntoArray(arr: ByteArray, destOff: Int = 0, startOff: Int = 0) =
        array.copyInto(arr, destOff, startOff, size)

    override fun copy(): ByteVec =
        ByteVec(size).also {
            copyIntoArray(it.array)
        }

    override fun reserve(amount: Int)  {
        if (amount > 0 && cap - size >= amount)
            return
        array = array.copyOf(size + amount)
        cap = size + amount
    }

    override fun popBack(): Byte =
        array[size - 1].also {
            reserve(-1)
            size --
        }

    fun tryPopPack(dest: ByteArray, destOff: Int = 0): Int {
        val can = kotlin.math.min(size, dest.size - destOff)
        copyIntoArray(dest, destOff, size - can)
        reserve(-can)
        size -= can
        return can
    }

    fun popBack(dest: ByteArray, destOff: Int = 0) {
        val destCopySize = dest.size - destOff
        require(size >= destCopySize)
        copyIntoArray(dest, destOff, size - destCopySize)
        reserve(-destCopySize)
        size -= destCopySize
    }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBack(batching: ByteArray, fn: (ByteArray, Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        while (true) {
            val rem = tryPopPack(batching)
            if (rem == 0) break

            fn(batching, rem)
        }
    }

    inline fun consumePopBack(batching: ByteArray, fn: (Byte) -> Unit) =
        consumePopBack(batching) { batch, count ->
            repeat(count) {
                fn(batch[it])
            }
        }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBackSlicedBatches(batching: ByteArray, fn: (ByteArray) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        while (true) {
            val rem = tryPopPack(batching)
            if (rem == 0) break

            if (rem == batching.size)
                fn(batching)
            else
                fn(batching.copyOf(rem))
        }
    }

    override fun get(index: Int): Byte =
        array[index]

    override fun flip() {
        array = array.reversedArray()
    }

    fun pushBack(arr: ByteArray) {
        reserve(arr.size)
        arr.copyInto(array, size)
        size += arr.size
    }

    override fun pushBack(elem: Byte) {
        reserve(8)
        array[size] = elem
        size ++
    }

    override fun iterator(): ByteBatchIterator =
        array.asSequence().asBatch().iterator().asByteBatchIterator()

    override fun toString(): String =
        joinToString(prefix = "[", postfix = "]") { "0x${it.toUByte().toString(16)}" }

    override fun set(index: Int, value: Byte) {
        array[index] = value
    }

    companion object {
        fun from(bytes: ByteArray) =
            ByteVec(bytes.size).also {
                bytes.copyInto(it.array)
                it.size += bytes.size
            }

        fun from(bytes: Sequence<Byte>) =
            ByteVec().also { bv ->
                bytes.forEach(bv::pushBack)
            }
    }
}