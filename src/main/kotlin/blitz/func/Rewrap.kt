package blitz.func

fun <T> Iterable<Monad<T>>.rewrap(): Monad<Sequence<T>> =
    Monad {
        val iter = this@rewrap.iterator()
        generateSequence {
            if (iter.hasNext())iter.next().impure()
            else null
        }
    }

fun <T> Sequence<Monad<T>>.rewrap(): Monad<Sequence<T>> =
    Monad {
        val iter = this@rewrap.iterator()
        sequence { if (iter.hasNext()) yield(iter.next().impure()) }
    }

fun <A, B> Monad<Pair<A, B>>.rewrap(): Pair<Monad<A>, Monad<B>> {
    val v = lazy { impure() }
    return Monad { v.value.first } to Monad { v.value.second }
}

fun <A, B> Pair<Monad<A>, Monad<B>>.rewrap(): Monad<Pair<A, B>> =
    Monad { first.impure() to second.impure() }

fun <A, B, C> Monad<Triple<A, B, C>>.rewrap(): Triple<Monad<A>, Monad<B>, Monad<C>> {
    val v = lazy { impure() }
    return Monad { v.value.first } to Monad { v.value.second } tri Monad { v.value.third }
}

fun <A, B, C> Triple<Monad<A>, Monad<B>, Monad<C>>.rewrap(): Monad<Triple<A, B, C>> =
    Monad { first.impure() to second.impure() tri third.impure() }