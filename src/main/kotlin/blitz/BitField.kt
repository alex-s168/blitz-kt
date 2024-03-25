package blitz

import blitz.collections.BitVec
import kotlin.reflect.KProperty

abstract class BitField {
    private var vec = BitVec(1)

    protected class BitDelegate(
        private val thi: BitField,
        private val pos: Int,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
            thi.vec[pos]

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            thi.vec[pos] = value
        }
    }

    protected fun bit(pos: Int): BitDelegate =
        BitDelegate(this, pos)

    fun decode(byte: Byte) {
        vec = BitVec.from(byteArrayOf(byte))
    }

    fun encode(): Byte =
        vec.toBytes()[0]

    override fun toString(): String =
        "${this::class.simpleName}(0b${vec.toBytes()[0].toString(2)})"
}