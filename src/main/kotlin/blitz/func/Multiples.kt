package blitz.func

infix fun <A, B, C> Pair<A, B>.tri(c: C): Triple<A, B, C> =
    Triple(first, second, c)