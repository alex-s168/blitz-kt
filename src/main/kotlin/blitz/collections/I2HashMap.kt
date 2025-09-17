package blitz.collections

import blitz.math.cantor

class I2HashMapKey(
    val a: Int,
    val b: Int
): Comparable<I2HashMapKey> {
    val cantorHash = cantor(a, b)
    override fun toString() =
        "($a, $b)"
    override fun hashCode(): Int =
        cantorHash
    override fun equals(other: Any?) =
        other is I2HashMapKey && other.a == a && other.b == b
    override fun compareTo(other: I2HashMapKey): Int =
        cantorHash.compareTo(other.cantorHash)
}

fun <V> I2HashMap(
    underlying: () -> MutableList<ListBuckets.Entry<I2HashMapKey, V>>,
    bucketCount: Int = 16,
): BlitzHashMap<I2HashMapKey, V> =
    BlitzHashMap(
        bucketCount,
        DynBucketsT<I2HashMapKey, V, _>(SortedListBuckets(underlying)),
        I2HashMapKey::cantorHash
    )