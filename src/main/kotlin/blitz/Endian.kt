package blitz

enum class Endian {
    LITTLE,
    BIG
    ;

    infix fun encodeLittle(little: ByteArray) =
        if (this == BIG) little.reversedArray()
        else little
}

fun Long.toBytes(endian: Endian) =
    endian encodeLittle
            toInt().toBytes(Endian.LITTLE) +
            toInt().shr(32).toBytes(Endian.LITTLE)

fun Int.toBytes(endian: Endian) =
    endian encodeLittle byteArrayOf(
        this.and(0xFF).toByte(),
        this.shr(8).and(0xFF).toByte(),
        this.shr(16).and(0xFF).toByte(),
        this.shr(24).and(0xFF).toByte()
    )

fun UInt.toBytes(endian: Endian) =
    toInt().toBytes(endian)

fun Short.toBytes(endian: Endian) =
    endian encodeLittle toInt().toBytes(Endian.LITTLE).copyOf(2)

fun UShort.toBytes(endian: Endian) =
    toShort().toBytes(endian)

fun Byte.toBytes() =
    byteArrayOf(this)

fun UByte.toBytes() =
    toByte().toBytes()