package blitz

fun Byte.toBool(): Boolean =
    (this.toInt() != 0)

fun Boolean.toByte(): Byte =
    if (this) 1 else 0

fun Boolean.toBit2(): Char =
    if (this) '1' else '0'