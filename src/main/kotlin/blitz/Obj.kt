package blitz

data class Obj<T>(val v: T)

fun <I, O> Obj<I>?.mapNotNull(transform: (I) -> O): Obj<O>? =
    this?.v?.let { Obj(transform(it)) }

fun <I, O> Obj<I>.map(transform: (I) -> O): Obj<O> =
    Obj(transform(v))

data class MutObj<T>(var v: T)