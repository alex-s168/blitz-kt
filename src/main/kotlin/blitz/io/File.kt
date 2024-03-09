package blitz.io

import blitz.ByteBatchSequence
import kotlinx.io.Buffer
import kotlinx.io.files.SystemFileSystem

@JvmInline
value class File internal constructor(
    val path: Path
)

fun File.read(): ByteBatchSequence =
    path.toKotlinxPath().let {
        { SystemFileSystem.source(it) }
    }.readerSequence()

private fun File.writeAppendFrom(seq: ByteBatchSequence, append: Boolean) {
    val path = path.toKotlinxPath()
    SystemFileSystem.sink(path, append).use { sink ->
        val iter = seq.iterator()
        while (iter.hasNext()) {
            val batch = iter.nextBytes(8192)
            val buff = Buffer()
            buff.write(batch)
            sink.write(buff, buff.size)
        }
    }
}

fun File.write(seq: ByteBatchSequence) =
    writeAppendFrom(seq, false)

fun File.append(seq: ByteBatchSequence) =
    writeAppendFrom(seq, true)