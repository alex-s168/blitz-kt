package blitz.collections

import blitz.math.cantor

class I3HashMapKey(
    val a: Int,
    val b: Int,
    val c: Int,
): Comparable<I3HashMapKey> {
    val cantorHash = cantor(a, cantor(b, c))
    override fun toString() =
        "($a, $b, $c)"
    override fun hashCode(): Int =
        cantorHash
    override fun equals(other: Any?) =
        other is I3HashMapKey && other.a == a && other.b == b && other.c == c
    override fun compareTo(other: I3HashMapKey): Int =
        cantorHash.compareTo(other.cantorHash)
}

fun <V> I3HashMap(
    underlying: () -> MutableList<ListBuckets.Entry<I3HashMapKey, V>>,
    bucketCount: Int = 16,
): BlitzHashMap<I3HashMapKey, V> =
    BlitzHashMap(
        bucketCount,
        DynBucketsT<_, _, _>(SortedListBuckets(underlying)),
        I3HashMapKey::cantorHash
    )