package blitz.func

fun Sequence<Monad<Unit>>.combine(): Monad<Unit> =
    Monad { this@combine.forEach { it.impure() } }

fun Iterable<Monad<Unit>>.combineIter(): Monad<Unit> =
    Monad { this@combineIter.forEach { it.impure() } }

fun Monad<Sequence<Monad<Unit>>>.combine(): Monad<Unit> =
    Monad { this@combine.impure().forEach { it.impure() } }

fun Monad<Iterable<Monad<Unit>>>.combineIter(): Monad<Unit> =
    Monad { this@combineIter.impure().forEach { it.impure() } }