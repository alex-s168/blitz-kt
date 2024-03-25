package blitz.collections

fun Array<Byte>.copyInto(dest: ByteArray) {
    forEachIndexed { i, byte -> dest[i] = byte }
}