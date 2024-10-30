package blitz.collections

class LightCache<K: Any, V>(
    private val keys: Vec<K>,
    private val vals: Vec<V>,
) {
    private var lastKey: K? = null
    private var lastVal: V? = null

    @Suppress("UNCHECKED_CAST")
    fun getOrPut(key: K, compute: (K) -> V): V {
        if (key == lastKey)
            return (lastVal as V)
        val idx = keys.indexOf(key)
        val v = if (idx >= 0) {
            vals[idx]
        } else {
            val x = compute(key)
            keys.pushBack(key)
            vals.pushBack(x)
            x
        }
        lastKey = key
        lastVal = v
        return v
    }

    internal fun getOrNullInternal(key: K): V? {
        if (key == lastKey)
            return lastVal
        val idx = keys.indexOf(key)
        return if (idx >= 0) {
            lastKey = key
            lastVal = vals[idx]
            lastVal
        } else {
            null
        }
    }

    companion object {
        inline fun <reified K: Any, reified V> new(initCap: Int = 0): LightCache<K, V> =
            LightCache(
                SmartVec<K>(initCap),
                SmartVec<V>(initCap),
            )
    }
}

fun <K: Any, V: Any> LightCache<K, V>.getOrNull(key: K): V? =
    getOrNullInternal(key)