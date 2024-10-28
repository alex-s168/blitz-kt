package blitz

fun unreachable(): Nothing =
    error("this should be unreachable")

inline fun <reified R> Any?.cast(): R? =
    this?.let { if (it is R) it else null }