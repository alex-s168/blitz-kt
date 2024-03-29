package blitz.collections

fun <T, G> Iterable<T>.mergeNeighbors(
    to: MutableList<Pair<G, MutableList<T>>> = mutableListOf(),
    by: (T) -> G
): MutableList<Pair<G, MutableList<T>>> {
    val out = mutableListOf<Pair<G, MutableList<T>>>()
    forEach {
        val b = by(it)
        if (b == out.lastOrNull()?.first)
            out.last().second.add(it)
        else
            out.add(b to mutableListOf(it))
    }
    return out
}

fun <T, G> Sequence<T>.mergeNeighbors(
    to: MutableList<Pair<G, MutableList<T>>> = mutableListOf(),
    by: (T) -> G
): MutableList<Pair<G, MutableList<T>>> =
    asIterable().mergeNeighbors(to, by)