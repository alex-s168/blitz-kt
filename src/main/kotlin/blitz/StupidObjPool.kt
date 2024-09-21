package blitz

import blitz.collections.RefVec
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * all created objects are stored in this pool, and when you call [StupidObjPool.markClear], all objects are marked as free and can be re-used
 * this is useful for when you need a ton of objects during a process and the process runs multiple times (caches the objects between executions)
 */
class StupidObjPool<T>(initialCap: Int) {
    @JvmField val _objs = RefVec<T>(initialCap)

    @JvmField var _nextFreeId = 0

    /** only one of constructor or initializer is called */
    @OptIn(ExperimentalContracts::class)
    inline fun get(constructor: () -> T, initializer: (T) -> Unit): T {
        contract {
            callsInPlace(constructor, InvocationKind.AT_MOST_ONCE)
            callsInPlace(initializer, InvocationKind.AT_MOST_ONCE)
        }

        if (_nextFreeId < _objs.size) {
            val o = _objs[_nextFreeId++]
            initializer(o)
            return o
        }

        val o = constructor()
        _objs.pushBack(o)
        _nextFreeId ++
        return o
    }

    fun clearPool() {
        _objs.clear()
        _nextFreeId = 0
    }

    fun markClear() {
        _nextFreeId = 0
    }
}