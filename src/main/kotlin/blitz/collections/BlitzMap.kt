package blitz.collections

interface BlitzMap<K,V,I> {
    fun index(key: K): I
    operator fun get(index: I): V?
    operator fun set(index: I, value: V?)
    fun contents(): Contents<Pair<K,V>>
}

fun <K,V,I> BlitzMap<K,V,I>.remove(index: I) =
    set(index, null)