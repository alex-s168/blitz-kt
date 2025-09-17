package blitz.collections

interface Buckets<K, V, B> {
    fun new(): B
    fun get(bucket: B, key: K): V?
    fun set(bucket: B, key: K, value: V)
    fun remove(bucket: B, key: K)
    fun contents(bucket: B): Contents<Pair<K,V>>
}

class DynBucketsT<K, V, B: Any?>(
    private val parent: Buckets<K, V, B>
): Buckets<K, V, Any?> {
    override fun new(): Any? =
        parent.new()

    override fun contents(bucket: Any?): Contents<Pair<K, V>> =
        parent.contents(bucket as B)

    override fun remove(bucket: Any?, key: K) =
        parent.remove(bucket as B, key)

    override fun set(bucket: Any?, key: K, value: V) =
        parent.set(bucket as B, key, value)

    override fun get(bucket: Any?, key: K): V? =
        parent.get(bucket as B, key)
}

typealias DynBuckets<K,V> = DynBucketsT<K,V,*>

class ListBuckets<K, V>(
    private val eq: (K, K) -> Boolean = { a, b -> a == b },
    private val construct: () -> MutableList<ListBuckets.Entry<K, V>>
): Buckets<K, V, MutableList<ListBuckets.Entry<K, V>>> {
    class Entry<K, V>(val key: K, var value: V): Comparable<ListBuckets.Entry<K, V>> {
        override fun compareTo(other: ListBuckets.Entry<K, V>): Int =
            (key as? Comparable<K>)?.compareTo(other.key) ?: 0

        override fun equals(other: Any?): Boolean =
            key == other
    }

    private fun entry(bucket: MutableList<Entry<K, V>>, key: K) =
        bucket.firstOrNull { eq(it.key, key) }

    override fun get(bucket: MutableList<Entry<K, V>>, key: K): V? =
        entry(bucket, key)?.value

    override fun set(bucket: MutableList<Entry<K, V>>, key: K, value: V) {
        val entry = entry(bucket, key)
        if (entry != null) {
            entry.value = value
        } else {
            bucket.add(Entry(key, value))
        }
    }

    override fun remove(bucket: MutableList<Entry<K, V>>, key: K) {
        entry(bucket, key)?.let(bucket::remove)
    }

    override fun new(): MutableList<Entry<K, V>> =
        construct()

    override fun contents(bucket: MutableList<Entry<K, V>>): Contents<Pair<K, V>> =
        bucket.map { it.key to it.value }.contents
}

fun <K: Comparable<K>, V> SortedListBuckets(
    underlying: () -> MutableList<ListBuckets.Entry<K, V>>,
    eq: (K, K) -> Boolean = { a, b -> a == b },
) = ListBuckets(eq) {
    SortedList(underlying()) { it }
}