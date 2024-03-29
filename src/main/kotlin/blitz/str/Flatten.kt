package blitz.str

fun Sequence<String>.flattenToString(): String {
    val out = StringBuilder()
    forEach {
        out.append(it)
    }
    return out.toString()
}

fun Iterable<String>.flattenToString(): String {
    val out = StringBuilder()
    forEach {
        out.append(it)
    }
    return out.toString()
}

fun Iterator<Char>.collectToString(): String {
    val out = StringBuilder()
    forEachRemaining {
        out.append(it)
    }
    return out.toString()
}