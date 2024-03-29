package blitz.collections

/** Returns the element that is at all positions in the list (if there is one) */
fun <T> Iterable<T>.findCommon(): T? =
    firstOrNull()?.let { first ->
        if (all { it == first }) first
        else null
    }

/** Returns the element that is at all positions in the list (if there is one) */
fun <T> Sequence<T>.findCommon(): T? =
    firstOrNull()?.let { first ->
        if (all { it == first }) first
        else null
    }