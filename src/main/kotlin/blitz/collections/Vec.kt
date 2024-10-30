package blitz.collections

interface Vec<T>: IndexableSequence<T> {
    val size: Int

    fun flip()

    fun copy(): Vec<T>

    fun reserve(amount: Int)
    fun reserve(need: Int, totalIfRealloc: Int) {
        reserve(need)
    }

    fun pushBack(elem: T)
    fun pushBack(elems: Array<T>) {
        reserve(elems.size)
        elems.forEach(::pushBack)
    }
    fun pushBack(elems: Collection<T>) {
        reserve(elems.size)
        elems.forEach(::pushBack)
    }
    fun pushBack(elems: Vec<T>) {
        reserve(elems.size)
        elems.forEach(::pushBack)
    }

    fun popBack(): T
    fun popBack(dest: Array<T>) {
        var writer = 0
        repeat(dest.size) {
            dest[writer ++] = popBack()
        }
    }

    operator fun set(index: Int, value: T)

    fun clear()

    fun idx(value: T): Int {
        for (i in 0 until size) {
            if (this[i] == value)
                return i
        }
        return -1
    }
}