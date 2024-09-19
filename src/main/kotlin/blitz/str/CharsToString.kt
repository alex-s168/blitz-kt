package blitz.str

import blitz.collections.Vec

fun Collection<Char>.charsToString(): String =
    String(this.toCharArray())

fun Vec<Char>.charsToString(): String =
    String(CharArray(size) { this[it] })

@JvmName("charsToString_VecByte")
fun Vec<Byte>.charsToString(): String =
    String(CharArray(size) { this[it].toInt().toChar() })