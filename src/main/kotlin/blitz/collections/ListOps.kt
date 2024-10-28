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

fun <T> MutableList<T>.addFront(value: T) =
    add(0, value)

fun <T: Any> Iterable<T?>.countNotNull() =
    count { it != null }

fun <T> Iterable<Iterable<T>>.intersections(dest: MutableList<T> = mutableListOf()): MutableList<T> =
    reduce { acc, li -> acc.intersect(li) }
        .forEach { dest += it }
        .let { dest }

fun <T> Iterable<T>.removeAtIndexes(idc: Iterable<Int>, dest: MutableList<T> = mutableListOf()): MutableList<T> =
    filterIndexedTo(dest) { index, _ -> index !in idc }

fun <T> List<T>.gather(idc: Iterable<Int>): MutableList<T> {
    val dest = mutableListOf<T>()
    idc.forEach {
        dest += get(it)
    }
    return dest
}

fun <T> List<T>.before(idx: Int): List<T> =
    take(idx)

fun <T> List<T>.after(idx: Int): List<T> =
    drop(idx + 1)

inline fun <I, reified O> Collection<I>.mapToArray(fn: (I) -> O): Array<O> {
    val iter = this.iterator()
    return Array(this.size) {
        fn(iter.next())
    }
}

inline fun <I, reified O> Collection<I>.mapIndexedToArray(fn: (Int, I) -> O): Array<O> {
    val iter = this.iterator()
    return Array(this.size) {
        fn(it, iter.next())
    }
}