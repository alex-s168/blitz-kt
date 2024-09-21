package blitz.collections

@Suppress("UNCHECKED_CAST")
class RefVec<T>(private val initCap: Int = 0): Vec<T> {
    override var size = 0
    @JvmField var _cap = initCap
    @JvmField var _array: Array<Any?>? = if (initCap > 0) arrayOfNulls(initCap) else null

    override fun clear() {
        size = 0
        if (_array == null) return
        if (_array!!.size <= initCap) {
            _cap = _array!!.size
        } else {
            _cap = 0
            _array = null
        }
    }

    inline fun copyAsArray(): Array<Any?> =
        _array?.copyOfRange(0, size) ?: emptyArray()

    inline fun copyIntoArray(arr: Array<Any?>, destOff: Int = 0, startOff: Int = 0) =
        _array?.copyInto(arr, destOff, startOff, size)

    override inline fun copy(): RefVec<T> =
        RefVec<T>(size).also {
            it._array?.let { copyIntoArray(it) }
        }

    override fun reserve(amount: Int)  {
        if (amount > 0 && _cap - size >= amount)
            return
        if (_array == null) {
            _cap = size + amount
            _array = arrayOfNulls(_cap)
        } else {
            _array = _array!!.copyOf(size + amount)
            _cap = size + amount
        }
    }

    override fun reserve(need: Int, totalIfRealloc: Int)  {
        if (need > 0 && _cap - size >= need)
            return
        if (_array == null) {
            _cap = size + totalIfRealloc
            _array = arrayOfNulls(_cap)
        } else {
            _array = _array!!.copyOf(size + totalIfRealloc)
            _cap = size + totalIfRealloc
        }
    }

    override fun popBack(): T =
        _array!![size - 1].also {
            reserve(-1)
            size --
        } as T

    override inline fun get(index: Int): T =
        (_array as Array<Any?>)[index] as T

    override fun flip() {
        _array = _array?.reversedArray()
    }

    override fun pushBack(elem: T) {
        reserve(1, 8)
        this[size] = elem
        size ++
    }

    override fun iterator(): Iterator<T> =
        object : Iterator<T> {
            var index = 0
            override fun hasNext(): Boolean = index < size
            override fun next(): T {
                if (!hasNext())
                    throw NoSuchElementException()
                return _array!![index++] as T
            }
        }

    override fun toString(): String =
        joinToString(prefix = "[", postfix = "]") { it.toString() }

    override fun set(index: Int, value: T) {
        (_array as Array<Any?>)[index] = value
    }

    companion object {
        fun <T> from(data: Array<T>) =
            RefVec<T>(data.size).also {
                it._array?.let { data.copyInto(it) }
                it.size += data.size
            }

        fun <T> from(data: Iterable<T>) =
            RefVec<T>().also { bv ->
                data.forEach(bv::pushBack)
            }

        inline fun <T> of(vararg elements: T): RefVec<T> =
            RefVec<T>(elements.size shl 2).also {
                it._array?.let { elements.copyInto(it) }
            }
    }
}