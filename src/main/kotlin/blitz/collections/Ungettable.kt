package blitz.collections

interface UnGettableIterator<T>: Iterator<T> {
    /**
     * Undo the previous next() operation. Only possible once per next()!
     */
    fun unGet()
}

fun <T> Iterator<T>.asUnGettable(): UnGettableIterator<T> =
    object: UnGettableIterator<T> {
        private var prev: T? = null
        private var unget = false

        override fun unGet() {
            if (prev == null)
                throw Exception("Cannot unGet because there was no previous")
            unget = true
        }

        override fun hasNext(): Boolean =
            unget || this@asUnGettable.hasNext()

        override fun next(): T =
            if (unget) prev!!.also { unget = false }
            else this@asUnGettable.next().also { prev = it }
    }