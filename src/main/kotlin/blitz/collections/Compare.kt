package blitz.collections

fun <T> List<T>.containsAt(at: Int, other: List<T>): Boolean {
    if (at + other.size > size)
        return false
    for (i in at..<at+other.size)
        if (this[i] != other[i-at])
            return false
    return true
}

fun <T> List<T>.startsWith(other: List<T>): Boolean =
    containsAt(0, other)

fun String.startsWith(re: Regex): Boolean =
    re.matchesAt(this, 0)

fun String.substringAfter(m: MatchResult): String =
    this.drop(m.value.length)

fun String.substringAfter(re: Regex): String? =
    re.matchAt(this, 0)
        ?.let(this::substringAfter)