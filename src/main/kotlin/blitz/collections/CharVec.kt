package blitz.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class CharVec(private val initCap: Int = 0): Vec<Char>, BatchSequence<Char> {
    override var size = 0
    private var cap = initCap
    private var array = CharArray(initCap)

    override fun clear() {
        size = 0
        if (array.size <= initCap) {
            cap = array.size
        } else {
            cap = initCap
            array = CharArray(initCap)
        }
    }

    fun copyAsArray(): CharArray =
        array.copyOfRange(0, size)

    fun copyIntoArray(arr: CharArray, destOff: Int = 0, startOff: Int = 0) =
        array.copyInto(arr, destOff, startOff, size)

    override fun copy(): CharVec =
        CharVec(size).also {
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

    override fun popBack(): Char =
        array[size - 1].also {
            reserve(-1)
            size --
        }

    fun tryPopPack(dest: CharArray, destOff: Int = 0): Int {
        val can = kotlin.math.min(size, dest.size - destOff)
        copyIntoArray(dest, destOff, size - can)
        reserve(-can)
        size -= can
        return can
    }

    fun popBack(dest: CharArray, destOff: Int = 0) {
        val destCopySize = dest.size - destOff
        require(size >= destCopySize)
        copyIntoArray(dest, destOff, size - destCopySize)
        reserve(-destCopySize)
        size -= destCopySize
    }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBack(batching: CharArray, fn: (CharArray, Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        while (true) {
            val rem = tryPopPack(batching)
            if (rem == 0) break

            fn(batching, rem)
        }
    }

    inline fun consumePopBack(batching: CharArray, fn: (Char) -> Unit) =
        consumePopBack(batching) { batch, count ->
            repeat(count) {
                fn(batch[it])
            }
        }

    @OptIn(ExperimentalContracts::class)
    inline fun consumePopBackSlicedBatches(batching: CharArray, fn: (CharArray) -> Unit) {
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

    override fun get(index: Int): Char =
        array[index]

    override fun flip() {
        array = array.reversedArray()
    }

    fun pushBack(arr: CharArray) {
        reserve(arr.size)
        arr.copyInto(array, size)
        size += arr.size
    }

    override fun pushBack(elem: Char) {
        reserve(1, 8)
        array[size] = elem
        size ++
    }

    override fun iterator(): BatchIterator<Char> =
        array.asSequence().asBatch().iterator()

    override fun toString(): String =
        String(array, 0, size)

    fun subViewToString(from: Int, num: Int = size - from): String =
        String(array, from, num)

    override fun set(index: Int, value: Char) {
        array[index] = value
    }

    override fun idx(value: Char): Int =
        array.indexOf(value)

    companion object {
        fun from(data: String) =
            CharVec(data.length).also {
                data.toCharArray().copyInto(it.array)
                it.size = data.length
            }

        fun from(bytes: CharArray) =
            CharVec(bytes.size).also {
                bytes.copyInto(it.array)
                it.size += bytes.size
            }

        fun from(bytes: Sequence<Char>) =
            CharVec().also { bv ->
                bytes.forEach(bv::pushBack)
            }
    }
}