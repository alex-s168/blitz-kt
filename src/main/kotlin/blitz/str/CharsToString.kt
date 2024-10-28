package blitz.str

import blitz.collections.ByteVec
import blitz.collections.CharVec
import blitz.collections.Vec

fun Collection<Char>.charsToString(): String =
    String(this.toCharArray())

fun Vec<Char>.charsToString(): String =
    when (this) {
        is CharVec -> subViewToString(0)
        else -> String(CharArray(size) { this[it] })
    }

@JvmName("charsToString_VecByte")
fun Vec<Byte>.charsToString(): String =
    when (this) {
        is ByteVec -> String(unsafeBackingArr())
        else -> String(CharArray(size) { this[it].toInt().toChar() })
    }