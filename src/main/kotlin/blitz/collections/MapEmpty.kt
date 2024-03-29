package blitz.collections

fun <T> Collection<T>.nullIfEmpty(): Collection<T>? =
    ifEmpty { null }

fun String.nullIfEmpty(): String? =
    ifEmpty { null }