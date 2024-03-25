package blitz.collections

fun <T, R> Iterator<T>.mapModifier(fn: (T) -> R): Iterator<R> =
    object : Iterator<R> {
        override fun next(): R =
            fn(this@mapModifier.next())

        override fun hasNext(): Boolean =
            this@mapModifier.hasNext()
    }