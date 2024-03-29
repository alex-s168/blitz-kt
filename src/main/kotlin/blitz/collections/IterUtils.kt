package blitz.collections

fun <T, R> Iterator<T>.mapModifier(fn: (T) -> R): Iterator<R> =
    object : Iterator<R> {
        override fun next(): R =
            fn(this@mapModifier.next())

        override fun hasNext(): Boolean =
            this@mapModifier.hasNext()
    }

fun <T> generateIterator(fn: () -> T?): Iterator<T> =
    object: Iterator<T> {
        var calc = false
        var nx: T? = null

        override fun hasNext(): Boolean {
            if (!calc) {
                nx = fn()
                calc = true
            }
            return nx != null
        }

        override fun next(): T {
            if (!hasNext())
                throw Exception("iterator end")
            calc = false
            return nx!!
        }
    }

/**
 * Can't explain this function. Look at the source of [blitz.parse.tokenize] as an example
 */
fun <T, R> Iterator<T>.funnyMap(fn: (UnGettableIterator<T>) -> R?): Iterator<R> {
    val iter = asUnGettable()
    return generateIterator { fn(iter) }
}

fun <T> Iterator<T>.collect(to: MutableList<T> = mutableListOf()): MutableList<T> {
    forEachRemaining {
        to.add(it)
    }
    return to
}

fun <T> Iterator<T>.collect(to: Vec<T>): Vec<T> {
    forEachRemaining {
        to.pushBack(it)
    }
    return to
}