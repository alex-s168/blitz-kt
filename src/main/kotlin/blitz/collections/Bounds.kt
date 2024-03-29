package blitz.collections

fun <T> List<T>.inBounds(index: Int): Boolean {
    if (index >= size)
        return false
    if (index < 0)
        return false
    return true
}

fun <T> Vec<T>.inBounds(index: Int): Boolean {
    if (index >= size)
        return false
    if (index < 0)
        return false
    return true
}