package blitz

import java.nio.ByteBuffer
import java.nio.ByteOrder

enum class Endian {
    LITTLE,
    BIG
    ;

    infix fun encodeLittle(little: ByteArray) =
        if (this == BIG) little.reversedArray()
        else little

    fun toNIO(): ByteOrder =
        if (this == LITTLE) ByteOrder.LITTLE_ENDIAN
        else ByteOrder.BIG_ENDIAN
}

fun ByteBuffer.order(endian: Endian): ByteBuffer =
    order(endian.toNIO())

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

// TODO: no cheat

fun ByteArray.toShort(endian: Endian) =
    ByteBuffer.wrap(this).order(endian).getShort()

fun ByteArray.toUShort(endian: Endian) =
    toShort(endian).toUShort()

fun ByteArray.toInt(endian: Endian) =
    ByteBuffer.wrap(this).order(endian).getInt()

fun ByteArray.toUInt(endian: Endian) =
    toInt(endian).toUInt()

fun ByteArray.toLong(endian: Endian) =
    ByteBuffer.wrap(this).order(endian).getLong()

fun ByteArray.toULong(endian: Endian) =
    toLong(endian).toULong()

fun ByteArray.toByte() =
    this[0]

fun ByteArray.toUByte() =
    this[0].toUByte()