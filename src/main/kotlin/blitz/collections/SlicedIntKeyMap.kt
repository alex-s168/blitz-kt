package blitz.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class SlicedIntKeyMap<V: Any>: BlitzMap<Int,V, Int> {
    val vec = ArrayList<V?>()
    var vecOffset: Int? = null

    override fun index(key: Int) = key

    override fun get(index: Int): V? {
        if (vecOffset == null) return null

        if (index < vecOffset!!) return null

        return vec.getOrNull(index - vecOffset!!)
    }

    /** @return Changed */
    @OptIn(ExperimentalContracts::class)
    fun setIfNotPresent(index: Int, value: (Int) -> V?): Boolean {
        contract {
            callsInPlace(value, InvocationKind.AT_MOST_ONCE)
        }

        if (vecOffset == null) {
            vecOffset = index
            vec.add(value(index))
            return true
        } else if (index < vecOffset!!) {
            // prepend

            val diff = vecOffset!! - index
            repeat(diff) {
                vec.add(0, null)
            }
            vecOffset = index
            vec[0] = value(index)
            return true
        }

        val offPlusSize = vecOffset!! + vec.size
        if (index >= offPlusSize) {
            // append

            val diff = index - offPlusSize
            repeat(diff + 1) {
                vec.add(null)
            }
            vec[vec.size - 1] = value(index)
            return true
        }

        return false
    }

    @OptIn(ExperimentalContracts::class)
    fun computeIfAbsent(index: Int, fn: (Int) -> V): V {
        contract {
            callsInPlace(fn, InvocationKind.AT_MOST_ONCE)
        }

        var value: V? = null
        setIfNotPresent(index) {
            fn(it).also { value = it }
        }
        return vec[index - vecOffset!!] ?: let {
            val v = value ?: fn(index)
            vec[index - vecOffset!!] = v
            v
        }
    }

    override fun set(index: Int, value: V?) {
        if (!setIfNotPresent(index) { value }) {
            vec[index - vecOffset!!] = value
        }
    }

    override fun contents(): Contents<Pair<Int, V>> =
        vec.withIndex()
            .mapNotNull { (id, v) -> v?.let { id + vecOffset!! to it } }
            .contents

    fun countSet(): Int =
        vec.countNotNull()

    @OptIn(ExperimentalContracts::class)
    inline fun forEachSet(fn: (Int, V) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        if (vecOffset == null) return

        repeat(vec.size) { ko ->
            val v = vec[ko]

            v?.let { fn(ko + vecOffset!!, it) }
        }
    }
}