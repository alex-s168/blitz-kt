package blitz.io

import kotlinx.io.RawSource
import kotlinx.io.buffered
import blitz.collections.ByteBatchIterator
import blitz.collections.ByteBatchSequence
import blitz.Provider

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