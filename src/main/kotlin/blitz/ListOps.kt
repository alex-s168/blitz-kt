package blitz

fun <T> MutableList<T>.removeFirst(count: Int) {
    repeat(count) {
        removeFirst()
    }
}

fun <T> MutableList<T>.removeLast(count: Int) {
    repeat(count) {
        removeLast()
    }
}