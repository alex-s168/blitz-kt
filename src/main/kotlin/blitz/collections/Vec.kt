package blitz.collections

interface Vec<T>: IndexableSequence<T> {
    val size: Int

    fun flip()

    fun copy(): Vec<T>

    fun reserve(amount: Int)

    fun pushBack(elem: T)
    fun pushBack(elems: Array<T>) {
        reserve(elems.size)
        elems.forEach(::pushBack)
    }
    fun pushBack(elems: Collection<T>) {
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
}