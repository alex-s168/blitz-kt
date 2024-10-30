package blitz.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ShortVec(private val initCap: Int = 0): Vec<Short>, BatchSequence<Short> {
    override var size = 0
    private var cap = initCap
    private var array = ShortArray(initCap)

    override fun clear() {
        size = 0
        if (array.size <= initCap) {
            cap = array.size
        } else {
            cap = initCap
            array = ShortArray(initCap)
        }
    }

    fun copyAsArray(): ShortArray =
        array.copyOfRange(0, size)

    fun copyIntoArray(arr: ShortArray, destOff: Int = 0, startOff: Int = 0) =
        array.copyInto(arr, destOff, startOff, size)

    override fun copy(): ShortVec =
        ShortVec(size).also {
            copyIntoArray(it.array)
        }

    override fun reserve(amount: Int)  {
        if (amount > 0 && cap - size >= amount)
            return
        array = array.copyOf(size + amount)
        cap = size + amount
    }

    override fun reserve(need: Int, wantIfRealloc: Int)  {
        if (need > 0 && cap - size >= need)
            return
        cap = size + wantIfRealloc
        array = array.copyOf(cap)
    }

    override fun popBack(): Short =
        array[size - 1].also {
            reserve(-1)
            size --
        }

    fun tryPopPack(dest: ShortArray, destOff: Int = 0): Int {
        val can = kotlin.math.min(size, dest.size - destOff)
        copyIntoArray(dest, destOff, size - can)
        reserve(-can)
        size -= can
        return can
    }

    fun popBack(dest: ShortArray, destOff: Int = 0) {
        val destCopySize = dest.size - destOff
        require(size >= destCopySize)
        copyIntoArray(dest, destOff, size - destCopySize)
        reserve(-destCopySize)
        size -= destCopySize
    }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBack(batching: ShortArray, fn: (ShortArray, Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        while (true) {
            val rem = tryPopPack(batching)
            if (rem == 0) break

            fn(batching, rem)
        }
    }

    inline fun consumePopBack(batching: ShortArray, fn: (Short) -> Unit) =
        consumePopBack(batching) { batch, count ->
            repeat(count) {
                fn(batch[it])
            }
        }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBackSlicedBatches(batching: ShortArray, fn: (ShortArray) -> Unit) {
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

    override fun get(index: Int): Short =
        array[index]

    override fun flip() {
        array = array.reversedArray()
    }

    fun pushBack(arr: ShortArray) {
        reserve(arr.size)
        arr.copyInto(array, size)
        size += arr.size
    }

    override fun pushBack(elem: Short) {
        reserve(1, 8)
        array[size] = elem
        size ++
    }

    override fun iterator(): BatchIterator<Short> =
        array.asSequence().asBatch().iterator()

    override fun toString(): String =
        contents.toString()

    override fun set(index: Int, value: Short) {
        array[index] = value
    }

    override fun idx(value: Short): Int =
        array.indexOf(value)

    companion object {
        fun from(bytes: ShortArray) =
            ShortVec(bytes.size).also {
                bytes.copyInto(it.array)
                it.size += bytes.size
            }

        fun from(bytes: Sequence<Short>) =
            ShortVec().also { bv ->
                bytes.forEach(bv::pushBack)
            }
    }
}