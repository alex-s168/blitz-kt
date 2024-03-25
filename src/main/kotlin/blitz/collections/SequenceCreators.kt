package blitz.collections

import blitz.Obj
import blitz.Provider
import kotlin.math.max

fun <T> lazySequence(vararg init: Pair<Int, T>, default: Obj<T>?, f: (Int, (Int) -> T) -> T): IndexableSequence<T> =
    object : IndexableSequence<T> {
        val map = mutableMapOf(*init)

        var current: Int? = null

        fun comp(iIn: Int): T {
            val i = max(0, iIn)
            if (current == i)
                return (default ?: throw Exception("recursion detected")).v
            return map[i] ?: let {
                current = i
                val res = f(i, ::comp)
                map[i] = res
                current = null
                res
            }
        }

        override fun get(index: Int) = comp(index)

        override fun iterator(): Iterator<T> =
            object : Iterator<T> {
                override fun hasNext() = true

                private var i = 0

                override fun next(): T =
                    comp(i ++)
            }
    }

fun <T> easySequence(vararg init: Pair<Int, T?>, f: (Int, (Int) -> T?) -> T?): Sequence<T?> =
    lazySequence(*init, default = Obj.of(null)) { i, ff ->
        f(i) { index ->
            var indexC = index
            var v: T? = null
            while (indexC > 0 && v == null)
                v = ff(indexC --)
            v
        }
    }

fun <I, T> Sequence<I>.easyMappingSequence(
    vararg init: Pair<Int, T?>,
    f: (Int, (Int) -> T?, (Int) -> I) -> T?
): Sequence<T?> {
    val indexable = this.asIndexable()
    return easySequence(*init) { i, ff ->
        f(i, ff, indexable::get)
    }.limitBy(indexable)
        .removeNull()
}

fun <T> selfInitializingSequence(block: Provider<Sequence<T>>): Sequence<T> =
    object : Sequence<T> {
        val seq by lazy(block)

        inner class Iter : Iterator<T> {
            val iter = seq.iterator()

            override fun hasNext(): Boolean =
                iter.hasNext()

            override fun next(): T =
                iter.next()
        }

        override fun iterator(): Iterator<T> =
            Iter()
    }