package blitz.func

fun <T, R> Monad<Iterable<T>>.mapIter(transform: (T) -> R): Monad<Iterable<R>> =
    bind { it.map { x -> transform(x) } }

fun <T, R> Monad<Sequence<T>>.map(transform: (T) -> R): Monad<Sequence<R>> =
    bind { it.map { x -> transform(x) } }