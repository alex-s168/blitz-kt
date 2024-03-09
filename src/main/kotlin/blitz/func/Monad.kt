package blitz.func

class Monad<O> internal constructor(
    val impure: () -> O
)

fun <O> unit(v: O): Monad<O> =
    Monad { v }

fun unit(): Monad<Unit> =
    Monad { }

fun <I, O> Monad<I>.bind(op: (I) -> O): Monad<O> =
    Monad { op(this@bind.impure()) }