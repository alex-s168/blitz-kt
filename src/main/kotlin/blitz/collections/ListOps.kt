package blitz.collections

fun <T> MutableList<T>.removeFirst(count: Int) {
    repeat(count) {
        removeFirst()
    }
}

fun <T> MutableList<T>.removeFirstInto(count: Int, dest: MutableList<T> = mutableListOf()): MutableList<T> {
    repeat(count) {
        dest.add(removeFirst())
    }
    return dest
}

fun <T> MutableList<T>.removeLast(count: Int) {
    repeat(count) {
        removeLast()
    }
}

fun <T> MutableList<T>.removeLastInto(count: Int, dest: MutableList<T> = mutableListOf()): MutableList<T> {
    repeat(count) {
        dest.add(removeLast())
    }
    return dest
}