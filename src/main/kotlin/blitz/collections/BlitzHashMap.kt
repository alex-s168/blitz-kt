package blitz.collections

class BlitzHashMap<K, V>(
    private val bucketCount: Int = 16,
    private val bucketSrc: DynBuckets<K, V>,
    private val hash: (K) -> Int,
): BlitzMap<K, V, BlitzHashMap.Index<K, V>> {
    private val buckets = Array(bucketCount) { bucketSrc.new() }

    override fun index(key: K): Index<K,V> =
        IndexImpl(buckets[hash(key) % bucketCount], key)

    private inline fun index(idx: Index<K,V>) =
        idx as IndexImpl

    override operator fun get(idx: Index<K,V>): V? =
        index(idx).let { idx ->
            bucketSrc.get(idx.bucket, idx.key)
        }

    override operator fun set(idx: Index<K,V>, value: V?) {
        index(idx).let { idx ->
            if (value == null) {
                bucketSrc.remove(idx.bucket, idx.key)
            } else {
                bucketSrc.set(idx.bucket, idx.key, value)
            }
        }
    }

    sealed interface Index<K,V>

    private class IndexImpl<K,V>(
        val bucket: Any?,
        val key: K,
    ): Index<K,V>

    override val contents: Contents<Pair<K, V>>
        get() = buckets
            .map { bucketSrc.contents(it) }
            .reduce { acc, pairs -> acc + pairs }

    val bucketStats
        get() = Contents(buckets.map { bucketSrc.contents(it).count() })
}