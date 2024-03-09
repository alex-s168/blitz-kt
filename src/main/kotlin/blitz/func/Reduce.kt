package blitz.func

fun <S, T : S> Monad<Sequence<T>>.reduce(operation: (acc: S, T) -> S): Monad<S> =
    bind { it.reduce(operation) }

fun <S, T : S> Monad<Iterable<T>>.reduceIter(operation: (acc: S, T) -> S): Monad<S> =
    bind { it.reduce(operation) }

fun <T> Monad<Sequence<T>>.reduce(each: (T) -> Unit): Monad<Unit> =
    Monad { this@reduce.impure().forEach { each(it) } }


fun <T> Monad<Iterable<T>>.reduceIter(each: (T) -> Unit): Monad<Unit> =
    Monad { this@reduceIter.impure().forEach { each(it) } }