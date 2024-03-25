package blitz.func

import blitz.collections.ByteBatchSequence
import blitz.collections.stringify

fun <T> Monad<Sequence<Sequence<T>>>.flatten(): Monad<Sequence<T>> =
    bind { it.flatten() }

fun Monad<ByteBatchSequence>.stringify(batch: Int = 64): Monad<Sequence<String>> =
    bind { it.stringify(batch) }