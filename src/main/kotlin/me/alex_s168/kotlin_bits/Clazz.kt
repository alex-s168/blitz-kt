package me.alex_s168.kotlin_bits

import kotlinx.io.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.math.max
import kotlin.math.min

interface IndexableSequence<T>: Sequence<T> {
    operator fun get(index: Int): T
}

data class Obj<T>(val v: T)

fun <I, O> Obj<I>?.map(transform: (I) -> O): Obj<O>? =
    this?.v?.let { Obj(transform(it)) }

fun <I, O> Obj<I>.map(transform: (I) -> O): Obj<O> =
    Obj(transform(v))

data class MutObj<T>(var v: T)

typealias Provider<T> = () -> T

class Either<A, B> private constructor(
    private val a: Obj<A>?,
    private val b: Obj<B>?
) {
    fun getAOrNull(): A? =
        a?.v

    fun getA(): A =
        (a ?: throw Exception("Value of Either is not of type A!")).v

    fun getAOr(prov: Provider<A>): A =
        getAOrNull() ?: prov()

    fun getBOrNull(): B? =
        b?.v

    fun getB(): B =
        (b ?: throw Exception("Value of Either is not of type B!")).v

    fun getBOr(prov: Provider<B>): B =
        getBOrNull() ?: prov()

    val isA: Boolean =
        a != null

    val isB: Boolean =
        b != null

    fun <R> then(af: (A) -> R, bf: (B) -> R): R =
        if (isA) af(a!!.v) else bf(b!!.v)

    fun <RA> mapA(transform: (A) -> RA): Either<RA, B> =
        Either(a.map(transform), b)

    fun <RB> mapB(transform: (B) -> RB): Either<A, RB> =
        Either(a, b.map(transform))

    override fun toString(): String =
        if (isA) "Either<A>($a)"
        else "Either<B>($b)"

    companion object {
        fun <A, B> ofA(a: A): Either<A, B> =
            Either(Obj(a), null)

        fun <A, B> ofB(b: B): Either<A, B> =
            Either(null, Obj(b))
    }
}

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
    lazySequence(*init, default = Obj(null)) { i, ff ->
        f(i) { index ->
            var indexC = index
            var v: T? = null
            while (indexC > 0 && v == null)
                v = ff(indexC --)
            v
        }
    }

fun <I, T> Sequence<I>.easyMappingSequence(vararg init: Pair<Int, T?>, f: (Int, (Int) -> T?, (Int) -> I) -> T?): Sequence<T?> {
    val indexable = this.asIndexable()
    return easySequence(*init) { i, ff ->
        f(i, ff, indexable::get)
    }.limitBy(indexable)
         .removeNull()
}

fun <T> IndexableSequence<T>.modifier(mod: (Sequence<T>) -> Sequence<T>) =
    object : IndexableSequence<T> {
        val other = mod(this@modifier)

        override fun iterator(): Iterator<T> =
            other.iterator()

        override fun get(index: Int): T =
            this@modifier[index]
    }

fun <T> Sequence<T>.removeNull(): Sequence<T> =
    mapNotNull { it }

fun <T> IndexableSequence<T>.removeNull(): IndexableSequence<T> =
    modifier { it.removeNull() }

fun <A, B> Sequence<A>.limitBy(other: Sequence<B>): Sequence<A> =
    object : Sequence<A> {
        override fun iterator(): Iterator<A> =
            object : Iterator<A> {
                val s = this@limitBy.iterator()
                val o = other.iterator()

                override fun hasNext(): Boolean =
                    o.hasNext() && s.hasNext()

                override fun next(): A =
                    s.next().also { o.next() }
            }
    }

fun <A, B> IndexableSequence<A>.limitBy(other: Sequence<B>): IndexableSequence<A> =
    modifier { it.limitBy(other) }

fun <T> Sequence<T>.asIndexable(): IndexableSequence<T> =
    object : IndexableSequence<T> {
        val iter = this@asIndexable.iterator()
        val values = mutableListOf<T>()

        override fun get(index: Int): T {
            if (index >= values.size) {
                repeat(index + 1 - values.size) {
                    values.add(iter.next())
                }
            }
            return values[index]
        }

        override fun iterator(): Iterator<T> =
            object : Iterator<T> {
                var i = 0

                override fun hasNext(): Boolean =
                    i < values.size || iter.hasNext()

                override fun next(): T =
                    get(i ++)
            }
    }

typealias Operator<I, O> = (I) -> O

fun <T, O: Any> caching(tiedGet: Provider<T>, calc: (T) -> O) = object : Lazy<O> {
    private var lastTiedV = tiedGet()
    private var lastV: O? = null

    override val value: O get() {
        val nTied = tiedGet()
        if (lastTiedV != nTied) {
            lastTiedV = nTied
            lastV = calc(nTied)
            return lastV!!
        }
        if (lastV == null)
            lastV = calc(nTied)
        return lastV!!
    }

    override fun isInitialized(): Boolean =
        lastTiedV == tiedGet() && lastV != null
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

class OperationChain<I, O> private constructor(
    private val impl: Impl = Impl()
) {
    private var until = 0

    private class Impl {
        val seqe = mutableListOf<(Sequence<Any?>) -> Sequence<Any?>>()

        var finalized = false

        fun add(op: Operator<*, *>) {
            seqe += { seq: Sequence<Any?> ->
                seq.map(op as Operator<Any?, Any?>)
            }
        }

        fun addFlat(op: Operator<*, Sequence<*>>) {
            seqe += { seq: Sequence<Any?> ->
                seq.flatMap(op as Operator<Any?, Sequence<Any?>>)
            }
        }
    }

    fun <NO> map(op: Operator<O, NO>): OperationChain<I, NO> =
        OperationChain<I, NO>(impl.also { it.add(op) })
            .also { it.until = this.until + 1 }

    fun <NO> flatMap(op: Operator<O, Sequence<NO>>): OperationChain<I, NO> =
        OperationChain<I, NO>(impl.also { it.addFlat(op) })
            .also { it.until = this.until + 1 }

    fun <NO> map(op: OperationChain<O, NO>): OperationChain<I, NO> {
        if (!op.impl.finalized)
            throw Exception("Can not map un-finalized operation chain onto operation chain!")
        return flatMap(op::process)
    }

    fun <NO> modifier(op: Operator<Sequence<O>, Sequence<NO>>): OperationChain<I, NO> =
        OperationChain<I, NO>(impl.also { it.seqe.add(op as (Sequence<Any?>) -> Sequence<Any?>) })
            .also { it.until = this.until + 1 }

    fun finalize(): OperationChain<I, O> {
        if (impl.finalized)
            throw Exception("Can't finalize a finalized OperationChain!")
        impl.finalized = true
        return this
    }

    fun process(v: I): Sequence<O> =
        selfInitializingSequence {
            var seq = sequenceOf<Any?>(v)
            impl.seqe
                .asSequence()
                .take(until)
                .forEach { op ->
                    seq = op(seq)
                }
            seq as Sequence<O>
        }

    fun processAll(v: Sequence<I>): Sequence<O> =
        v.flatMap { process(it) }

    companion object {
        internal fun <I> create(): OperationChain<I, I> =
            OperationChain()
    }
}

class Contents<T> internal constructor(
    private val iterable: Iterable<T>
): Iterable<T> {
    override fun iterator(): Iterator<T> =
        iterable.iterator()

    override fun equals(other: Any?): Boolean {
        if (other !is Contents<*>)
            return false

        val it1 = this.iterable.iterator()
        val it2 = other.iterable.iterator()

        while (true) {
            val hasNext1 = it1.hasNext()
            val hasNext2 = it2.hasNext()

            if ((hasNext1 && !hasNext2) || (hasNext2 && !hasNext1))
                return false

            if (!hasNext1)
                return true

            if (it1.next() != it2.next())
                return false
        }
    }

    override fun hashCode(): Int =
        iterable.hashCode()

    override fun toString(): String =
        joinToString(
            separator = ", ",
            prefix = "[",
            postfix = "]"
        ) {
            it.toString()
        }
}

val <T> Iterable<T>.contents get() =
    Contents(this)

val <T> Sequence<T>.contents get() =
    Contents(this.asIterable())

val <T> Array<T>.contents get() =
    Contents(this.asIterable())

fun <T, O> Sequence<T>.map(chain: OperationChain<T, O>): Sequence<O> =
    chain.processAll(this)

fun <I> chain(): OperationChain<I, I> =
    OperationChain.create()

fun <I, O> OperationChain<I, O>.chunked(size: Int): OperationChain<I, List<O>> =
    modifier { it.chunked(size) }

fun <I, O, R> OperationChain<I, O>.chunked(size: Int, transform: (List<O>) -> R): OperationChain<I, R> =
    modifier { it.chunked(size, transform) }

fun <I, O> OperationChain<I, O>.filter(predicate: (O) -> Boolean): OperationChain<I, O> =
    modifier { it.filter(predicate) }

fun <T> MutableList<T>.removeFirst(count: Int) {
    repeat(count) {
        removeFirst()
    }
}

fun <T> MutableList<T>.removeLast(count: Int) {
    repeat(count) {
        removeLast()
    }
}

interface BatchIterator<T>: Iterator<T> {
    fun next(limit: Int): List<T>
    fun next(dest: Array<T>): Int
    fun next(dest: MutableList<T>, limit: Int)
}

interface ByteBatchIterator: BatchIterator<Byte> {
    fun nextBytes(limit: Int): ByteArray

    fun nextBytes(dest: ByteArray): Int
}

interface BatchSequence<T>: Sequence<T> {
    override fun iterator(): BatchIterator<T>
}

interface ByteBatchSequence: BatchSequence<Byte> {
    override fun iterator(): ByteBatchIterator
}

/**
 * Batches all get operations on the sequence.
 */
fun <T> BatchSequence<T>.batched(count: Int): BatchSequence<T> =
    object : BatchSequence<T> {
        inner class Iter: BatchIterator<T> {
            val parent = this@batched.iterator()

            var batch = mutableListOf<T>()

            override fun next(limit: Int): List<T> {
                if (!hasNext())
                    throw Exception("no next")

                val c = min(limit, batch.size)
                val ret = batch.take(c)
                batch.removeFirst(c)
                return ret
            }

            override fun next(dest: MutableList<T>, limit: Int) {
                if (!hasNext())
                    throw Exception("no next")

                val c = min(limit, batch.size)
                dest.addAll(batch.subList(0, max(0, c-1)))
                batch.removeFirst(c)
                return
            }

            override fun next(dest: Array<T>): Int {
                if (!hasNext())
                    throw Exception("no next")

                val c = min(dest.size, batch.size)
                batch.subList(0, max(0, c-1)).forEachIndexed { i, t ->
                    dest[i] = t
                }
                batch.removeFirst(c)
                return c
            }

            override fun next(): T {
                if (!hasNext())
                    throw Exception("no next")
                val v = batch.first()
                batch.removeFirst()
                return v
            }

            override fun hasNext(): Boolean {
                while (batch.isEmpty()) {
                    if (!parent.hasNext())
                        return false
                    parent.next(batch, count)
                }
                return true
            }
        }

        override fun iterator(): BatchIterator<T> =
            Iter()
    }

fun <T> Sequence<T>.asBatch(): BatchSequence<T> =
    object : BatchSequence<T> {
        inner class Iter: BatchIterator<T> {
            var iter = this@asBatch.iterator()

            override fun next(limit: Int): List<T> =
                mutableListOf<T>()
                    .also { next(it, limit) }

            override fun next(dest: MutableList<T>, limit: Int) {
                for (i in 0..<limit) {
                    if (!iter.hasNext())
                        break
                    dest.add(iter.next())
                }
            }

            override fun next(dest: Array<T>): Int {
                var i = 0
                while (i < dest.size) {
                    if (!iter.hasNext())
                        break
                    dest[i ++] = iter.next()
                }
                return i
            }

            override fun next(): T {
                return iter.next()
            }

            override fun hasNext(): Boolean {
                return iter.hasNext()
            }
        }

        override fun iterator(): BatchIterator<T> =
            Iter()
    }

/*
# Batched sequences
## Source
You should make all your sources return `BatchSequence<T>`
and then you can use the `.batched(count: Int)` function
to drastically decrease the amount of single reads in the original source.
Example:
```kt
File("text.txt")  // File
  .openRead()     // BatchSequence<Byte>
  .batched(64)    // BatchSequence<Byte>
```
with this, if `.openRead()` returns a dumb sequence that always only gets one byte at once,
you can speed up the reading process by a lot

## Sink
You should make all your sinks take `BatchSequence<T>`
and then you can use the `.asBatch()` function to allow
the sink to get multiple bytes at once
Example:
```kt
val data = myData  // Sequence<Byte>
  .asBatch()       // BatchSequence<Byte>

File("text.txt")
  .write(data)
```

# Lazy Sequences
When writing recursive functions like Fibonacci, it is often easier and faster to use
lazy sequences.
Example:
```kt
val fib = lazySequence(0 to 1) { i, f ->
  f(i-1) + f(i-2)
}

println(fib[10])
```
Note: If we call f for any number below 0, it will call f(0) instead.

# Operation Chains
TODO

# Contents
TODO

# Monads
TODO

# Easy Sequence
TODO

# Easy Mapping Sequence
TODO

# Obj and MutObj
TODO
 */

data class Monad<O>(
    val impure: () -> O
)

fun <O> unit(v: O): Monad<O> =
    Monad { v }

fun unit(): Monad<Unit> =
    Monad { }

fun <I, O> Monad<I>.bind(op: (I) -> O): Monad<O> =
    Monad { op(this@bind.impure()) }

fun Monad<String>.print() =
    bind { print(it) }

fun Monad<String>.asPath() =
    bind { Path(it) }

fun ByteBatchSequence.stringify(batch: Int = 64): Sequence<String> {
    val iter = iterator()
    return generateSequence {
        if (iter.hasNext())
            iter.nextBytes(batch).decodeToString()
        else null
    }
}

fun Provider<RawSource>.readerSequence(): ByteBatchSequence =
    object : ByteBatchSequence {
        inner class Iter: ByteBatchIterator {
            val buffered = this@readerSequence().buffered()

            override fun nextBytes(limit: Int): ByteArray {
                val out = ByteArray(limit)
                var i = 0
                while (!(buffered.exhausted() || i == limit - 1))
                    out[i ++] = buffered.readByte()
                return out.sliceArray(0..i)
            }

            override fun nextBytes(dest: ByteArray): Int =
                nextBytes(dest.size).also { it.copyInto(dest) }.size

            override fun next(limit: Int): List<Byte> =
                nextBytes(limit).toList()

            override fun next(dest: MutableList<Byte>, limit: Int) {
                for (x in nextBytes(limit)) {
                    dest.add(x)
                }
            }

            override fun next(dest: Array<Byte>): Int {
                var i = 0
                for (x in nextBytes(dest.size)) {
                    dest[i ++] = x
                }
                return i
            }

            override fun next(): Byte =
                buffered.readByte()

            override fun hasNext(): Boolean =
                !buffered.exhausted()
        }

        override fun iterator(): ByteBatchIterator =
            Iter()
    }

fun <T> Iterable<Monad<T>>.rewrap(): Monad<Sequence<T>> =
    Monad {
        val iter = this@rewrap.iterator()
        generateSequence {
            if (iter.hasNext())iter.next().impure()
            else null
        }
    }

fun <T> Sequence<Monad<T>>.rewrap(): Monad<Sequence<T>> =
    Monad {
        val iter = this@rewrap.iterator()
        sequence { if (iter.hasNext()) yield(iter.next().impure()) }
    }

fun Sequence<Monad<Unit>>.combine(): Monad<Unit> =
    Monad { this@combine.forEach { it.impure() } }

fun Iterable<Monad<Unit>>.combineIter(): Monad<Unit> =
    Monad { this@combineIter.forEach { it.impure() } }

fun Monad<Sequence<Monad<Unit>>>.combine(): Monad<Unit> =
    Monad { this@combine.impure().forEach { it.impure() } }

fun Monad<Iterable<Monad<Unit>>>.combineIter(): Monad<Unit> =
    Monad { this@combineIter.impure().forEach { it.impure() } }

fun <S, T : S> Monad<Sequence<T>>.reduce(operation: (acc: S, T) -> S): Monad<S> =
    bind { it.reduce(operation) }

fun <S, T : S> Monad<Iterable<T>>.reduceIter(operation: (acc: S, T) -> S): Monad<S> =
    bind { it.reduce(operation) }

fun <T> Monad<Sequence<T>>.reduce(each: (T) -> Unit): Monad<Unit> =
    Monad { this@reduce.impure().forEach { each(it) } }


fun <T> Monad<Iterable<T>>.reduceIter(each: (T) -> Unit): Monad<Unit> =
    Monad { this@reduceIter.impure().forEach { each(it) } }

fun <T, R> Monad<Iterable<T>>.mapIter(transform: (T) -> R): Monad<Iterable<R>> =
    bind { it.map { x -> transform(x) } }

fun <T, R> Monad<Sequence<T>>.map(transform: (T) -> R): Monad<Sequence<R>> =
    bind { it.map { x -> transform(x) } }

fun <T> Monad<Sequence<Sequence<T>>>.flatten(): Monad<Sequence<T>> =
    bind { it.flatten() }

fun Monad<ByteBatchSequence>.stringify(batch: Int = 64): Monad<Sequence<String>> =
    bind { it.stringify(batch) }

fun Monad<Path>.read() =
    bind { p -> { SystemFileSystem.source(p) }.readerSequence() }

fun readIn() =
    Monad { generateSequence { readln() } }

fun interface DeferScope {
    fun defer(block: () -> Unit)
}

interface ExecutionScope: DeferScope {
    fun onError(block: () -> Unit)
    
    fun error()

    companion object {
        fun create(
            defer: (block: () -> Unit) -> Unit,
            onError: (block: () -> Unit) -> Unit,
            error: () -> Unit,
        ) = object : ExecutionScope {
            override fun defer(block: () -> Unit) =
                defer(block)

            override fun onError(block: () -> Unit) =
                onError(block)

            override fun error() =
                error()
        }
    }
}

fun <R> resourceScoped(block: ExecutionScope.() -> R): R {
    val defer = mutableListOf<() -> Unit>()
    val onError = mutableListOf<() -> Unit>()
    val scope = ExecutionScope.create(defer::add, onError::add) {
        throw Exception("Manual error triggered")
    }
    val ex: Exception
    try {
        return block(scope)
    } catch (e: Exception) {
        ex = e
        onError.forEach { it.invoke() }
    } finally {
        defer.forEach { it.invoke() }
    }
    throw ex
}

/*
fun main() {
    val chain = chain<Int>()
        .map(Int::toString)
        .map(String::reversed)
        .finalize()
    println(chain.process(120).contents)
}*/
