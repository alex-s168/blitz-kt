package blitz.collections

fun <T> Sequence<T>.removeNull(): Sequence<T> =
    mapNotNull { it }

fun <T> IndexableSequence<T>.removeNull(): IndexableSequence<T> =
    modifier { it.removeNull() }

fun <A, B> Sequence<A>.limitBy(other: Sequence<B>): Sequence<A> =
    object : Sequence<A> {
        override fun iterator(): Iterator<A> =
            object : Iterator<A> {
                val s = this@limitBy.iterator()
                val o = other.iterator()

                override fun hasNext(): Boolean =
                    o.hasNext() && s.hasNext()

                override fun next(): A =
                    s.next().also { o.next() }
            }
    }

fun <A, B> IndexableSequence<A>.limitBy(other: Sequence<B>): IndexableSequence<A> =
    modifier { it.limitBy(other) }