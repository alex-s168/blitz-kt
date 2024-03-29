package blitz.collections

interface IndexableSequence<T>: Sequence<T> {
    operator fun get(index: Int): T
}

// TODO: rename to map
fun <T> IndexableSequence<T>.modifier(mod: (Sequence<T>) -> Sequence<T>) =
    object : IndexableSequence<T> {
        val other = mod(this@modifier)

        override fun iterator(): Iterator<T> =
            other.iterator()

        override fun get(index: Int): T =
            this@modifier[index]
    }

fun <T> Sequence<T>.asIndexable(): IndexableSequence<T> =
    object : IndexableSequence<T> {
        val iter = this@asIndexable.iterator()
        val values = mutableListOf<T>()

        override fun get(index: Int): T {
            if (index >= values.size) {
                repeat(index + 1 - values.size) {
                    values.add(iter.next())
                }
            }
            return values[index]
        }

        override fun iterator(): Iterator<T> =
            object : Iterator<T> {
                var i = 0

                override fun hasNext(): Boolean =
                    i < values.size || iter.hasNext()

                override fun next(): T =
                    get(i ++)
            }
    }