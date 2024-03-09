package blitz

class Contents<T> internal constructor(
    private val iterable: Iterable<T>
): Iterable<T> {
    override fun iterator(): Iterator<T> =
        iterable.iterator()

    override fun equals(other: Any?): Boolean {
        if (other !is Contents<*>)
            return false

        val it1 = this.iterable.iterator()
        val it2 = other.iterable.iterator()

        while (true) {
            val hasNext1 = it1.hasNext()
            val hasNext2 = it2.hasNext()

            if ((hasNext1 && !hasNext2) || (hasNext2 && !hasNext1))
                return false

            if (!hasNext1)
                return true

            if (it1.next() != it2.next())
                return false
        }
    }

    override fun hashCode(): Int =
        iterable.hashCode()

    override fun toString(): String =
        joinToString(
            separator = ", ",
            prefix = "[",
            postfix = "]"
        ) {
            it.toString()
        }
}


val <T> Iterable<T>.contents get() =
    Contents(this)

val <T> Sequence<T>.contents get() =
    Contents(this.asIterable())

val <T> Array<T>.contents get() =
    Contents(this.asIterable())