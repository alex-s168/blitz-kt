package me.alex_s168.kreflect

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readTo
import kotlin.math.max
import kotlin.math.min

interface IndexableSequence<T>: Sequence<T> {
    operator fun get(index: Int): T
}

fun <T> lazySequence(vararg init: Pair<Int, T>, f: (Int, (Int) -> T) -> T): IndexableSequence<T> =
    object : IndexableSequence<T> {
        val map = mutableMapOf(*init)

        fun comp(iIn: Int): T {
            val i = max(0, iIn)
            return map[i] ?: f(i, ::comp).also { map[i] = it }
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

typealias Operator<I, O> = (I) -> O

fun <T, O: Any> caching(tiedGet: () -> T, calc: (T) -> O) = object : Lazy<O> {
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

fun <T> selfInitializingSequence(block: () -> Sequence<T>): Sequence<T> =
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
 */

data class Monad<O>(
    val impure: () -> O
)

fun <O> unit(v: O): Monad<O> =
    Monad { v }

fun unit(): Monad<Unit> =
    Monad { Unit }

fun <I, O> Monad<I>.bind(op: (I) -> O): Monad<O> =
    Monad { op(this@bind.impure()) }

fun Monad<String>.print() =
    bind { print(it) }

fun Monad<String>.asPath() =
    bind { Path(it) }

fun ByteBatchSequence.stringify(batch: Int): Sequence<String> {
    val iter = iterator()
    return sequence {
        if (iter.hasNext())
            yield(iter.nextBytes(batch).decodeToString())
    }
}

fun (() -> RawSource).readerSequence(): ByteBatchSequence =
    object : ByteBatchSequence {
        inner class Iter: ByteBatchIterator {
            val src = this@readerSequence()
            val buffer = Buffer()

            override fun nextBytes(limit: Int): ByteArray {
                src.readAtMostTo(buffer, limit - buffer.size)
                return buffer.readByteArray()
            }

            override fun nextBytes(dest: ByteArray): Int {
                src.readAtMostTo(buffer, max(0, dest.size - buffer.size))
                val c = buffer.size
                buffer.readTo(dest)
                return min(c.toInt(), dest.size)
            }

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
                nextBytes(1).first()

            override fun hasNext(): Boolean {
                return src.readAtMostTo(buffer, 1) > 0
            }
        }

        override fun iterator(): ByteBatchIterator =
            Iter()
    }

fun Monad<Path>.read() =
    bind { p -> { SystemFileSystem.source(p) }.readerSequence() }

fun readIn() =
    Monad { generateSequence { readln() } }

/*
fun main() {
    val chain = chain<Int>()
        .map(Int::toString)
        .map(String::reversed)
        .finalize()
    println(chain.process(120).contents)
}*/