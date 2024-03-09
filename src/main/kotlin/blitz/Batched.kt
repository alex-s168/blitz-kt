package blitz

import kotlin.math.max
import kotlin.math.min

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