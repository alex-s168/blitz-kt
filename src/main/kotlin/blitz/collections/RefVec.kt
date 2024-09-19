package blitz.collections

import kotlin.system.measureTimeMillis

@Suppress("UNCHECKED_CAST")
class RefVec<T>(private val initCap: Int = 0): Vec<T> {
    override var size = 0
    private var cap = initCap
    private var array: Array<Any?>? = if (initCap > 0) arrayOfNulls(initCap) else null

    override fun clear() {
        size = 0
        if (array == null) return
        if (array!!.size <= initCap) {
            cap = array!!.size
        } else {
            cap = 0
            array = null
        }
    }

    fun copyAsArray(): Array<Any?> =
        array?.copyOfRange(0, size) ?: emptyArray()

    fun copyIntoArray(arr: Array<Any?>, destOff: Int = 0, startOff: Int = 0) =
        array?.copyInto(arr, destOff, startOff, size)

    override fun copy(): RefVec<T> =
        RefVec<T>(size).also {
            it.array?.let { copyIntoArray(it) }
        }

    override fun reserve(amount: Int)  {
        if (amount > 0 && cap - size >= amount)
            return
        if (array == null) {
            cap = size + amount
            array = arrayOfNulls(cap)
        } else {
            array = array!!.copyOf(size + amount)
            cap = size + amount
        }
    }

    override fun reserve(need: Int, totalIfRealloc: Int)  {
        if (need > 0 && cap - size >= need)
            return
        if (array == null) {
            cap = size + totalIfRealloc
            array = arrayOfNulls(cap)
        } else {
            array = array!!.copyOf(size + totalIfRealloc)
            cap = size + totalIfRealloc
        }
    }

    override fun popBack(): T =
        array!![size - 1].also {
            reserve(-1)
            size --
        } as T

    override fun get(index: Int): T =
        array!![index] as T

    override fun flip() {
        array = array?.reversedArray()
    }

    override fun pushBack(elem: T) {
        reserve(1, 8)
        array!![size] = elem
        size ++
    }

    override fun iterator(): Iterator<T> =
        object : Iterator<T> {
            var index = 0
            override fun hasNext(): Boolean = index < size
            override fun next(): T {
                if (!hasNext())
                    throw NoSuchElementException()
                return array!![index++] as T
            }
        }

    override fun toString(): String =
        joinToString(prefix = "[", postfix = "]") { it.toString() }

    override fun set(index: Int, value: T) {
        array!![index] = value
    }

    companion object {
        fun <T> from(data: Array<T>) =
            RefVec<T>(data.size).also {
                it.array?.let { data.copyInto(it) }
                it.size += data.size
            }

        fun <T> from(data: Iterable<T>) =
            RefVec<T>().also { bv ->
                data.forEach(bv::pushBack)
            }

        fun <T> of(vararg elements: T): RefVec<T> =
            RefVec<T>(elements.size).also {
                it.array?.let { elements.copyInto(it) }
            }
    }
}