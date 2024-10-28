package blitz.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class IntVec(private val initCap: Int = 0): Vec<Int>, BatchSequence<Int> {
    override var size = 0
    private var cap = initCap
    private var array = IntArray(initCap)

    override fun clear() {
        size = 0
        if (array.size <= initCap) {
            cap = array.size
        } else {
            cap = initCap
            array = IntArray(initCap)
        }
    }

    fun copyAsArray(): IntArray =
        array.copyOfRange(0, size)

    fun copyIntoArray(arr: IntArray, destOff: Int = 0, startOff: Int = 0) =
        array.copyInto(arr, destOff, startOff, size)

    override fun copy(): IntVec =
        IntVec(size).also {
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

    override fun popBack(): Int =
        array[size - 1].also {
            reserve(-1)
            size --
        }

    fun tryPopPack(dest: IntArray, destOff: Int = 0): Int {
        val can = kotlin.math.min(size, dest.size - destOff)
        copyIntoArray(dest, destOff, size - can)
        reserve(-can)
        size -= can
        return can
    }

    fun popBack(dest: IntArray, destOff: Int = 0) {
        val destCopySize = dest.size - destOff
        require(size >= destCopySize)
        copyIntoArray(dest, destOff, size - destCopySize)
        reserve(-destCopySize)
        size -= destCopySize
    }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBack(batching: IntArray, fn: (IntArray, Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        while (true) {
            val rem = tryPopPack(batching)
            if (rem == 0) break

            fn(batching, rem)
        }
    }

    inline fun consumePopBack(batching: IntArray, fn: (Int) -> Unit) =
        consumePopBack(batching) { batch, count ->
            repeat(count) {
                fn(batch[it])
            }
        }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBackSlicedBatches(batching: IntArray, fn: (IntArray) -> Unit) {
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

    override fun get(index: Int): Int =
        array[index]

    override fun flip() {
        array = array.reversedArray()
    }

    fun pushBack(arr: IntArray) {
        reserve(arr.size)
        arr.copyInto(array, size)
        size += arr.size
    }

    override fun pushBack(elem: Int) {
        reserve(1, 8)
        array[size] = elem
        size ++
    }

    override fun iterator(): BatchIterator<Int> =
        array.asSequence().asBatch().iterator()

    override fun toString(): String =
        contents.toString()

    override fun set(index: Int, value: Int) {
        array[index] = value
    }

    companion object {
        fun from(bytes: IntArray) =
            IntVec(bytes.size).also {
                bytes.copyInto(it.array)
                it.size += bytes.size
            }

        fun from(bytes: Sequence<Int>) =
            IntVec().also { bv ->
                bytes.forEach(bv::pushBack)
            }
    }
}