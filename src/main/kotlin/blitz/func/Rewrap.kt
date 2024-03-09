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
