package blitz.collections

import blitz.Endian
import blitz.toBytes
import blitz.toInt
import blitz.toShort
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class DenseIx16x16BoolMap {
    val backing = SlicedIntKeyMap<Dense16x16BoolMap>()

    operator fun get(y: Int) =
        backing[y]

    operator fun get(x: Int, y: Int, z: Int): Boolean =
        backing[y]?.get(x, z) ?: false

    fun getOrCreateLayer(y: Int) =
        @OptIn(ExperimentalUnsignedTypes::class)
        backing.computeIfAbsent(y) { Dense16x16BoolMap() }

    operator fun set(x: Int, y: Int, z: Int, value: Boolean) {
        getOrCreateLayer(y)[x, z] = value
    }

    @OptIn(ExperimentalContracts::class)
    inline fun forEachSet(fn: (Int, Int, Int) -> Unit) {
        contract {
            callsInPlace(fn)
        }

        val layerBytes = ByteVec()
        val buf32 = ByteArray(32)
        backing.forEachSet { y, layer ->
            layer.packedSetPosList(layerBytes)
            Dense16x16BoolMap.consumeAllPackedPos(layerBytes, buf32) { x, z ->
                fn(x, y, z)
            }
        }
    }

    inline fun <T> getSetAsSequence(crossinline convertIndex: (Int, Int, Int) -> T) =
        sequence {
            forEachSet { x, y, z ->
                yield(convertIndex(x, y, z))
            }
        }

    /**
     * base: 4
     * per contained layer: (4 or 2) + 1 + (0 to 256) bytes
     * only recommended if very few positions per layer
     */
    fun serializeByPositions(yPosAsWord: Boolean, unbufferedConsumer: (ByteArray) -> Unit) {
        val layerBytes = ByteVec()
        val buf32 = ByteArray(32)

        backing.countSet().toBytes(Endian.LITTLE).also(unbufferedConsumer)

        backing.forEachSet { y, layer ->
            if (yPosAsWord) {
                y.toShort().toBytes(Endian.LITTLE).also(unbufferedConsumer)
            } else {
                y.toBytes(Endian.LITTLE).also(unbufferedConsumer)
            }

            layer.packedSetPosList(layerBytes)
            layerBytes.size.toUByte().toBytes().also(unbufferedConsumer)
            layerBytes.consumePopBackSlicedBatches(buf32, unbufferedConsumer)
        }
    }

    /**
     * base: 4
     * per contained layer: (4 or 2) + 32 bytes
     * should almost always be used
     */
    fun serializeByLayers(yPosAsWord: Boolean, unbufferedConsumer: (ByteArray) -> Unit) {
        backing.countSet().toBytes(Endian.LITTLE).also(unbufferedConsumer)

        val buf32 = ByteArray(32)
        backing.forEachSet { y, layer ->
            if (yPosAsWord) {
                y.toShort().toBytes(Endian.LITTLE).also(unbufferedConsumer)
            } else {
                y.toBytes(Endian.LITTLE).also(unbufferedConsumer)
            }

            layer.getPackedBytes(buf32)
            unbufferedConsumer(buf32)
        }
    }

    companion object {
        fun deserializeByPositions(yPosAsWord: Boolean, appendTo: DenseIx16x16BoolMap = DenseIx16x16BoolMap(), unbufferedProvider: (Int) -> ByteArray): DenseIx16x16BoolMap {
            val count = unbufferedProvider(4).toInt(Endian.LITTLE)

            repeat(count) {
                val (ypos, layerByteCount) = if (yPosAsWord) {
                    val byteArr = unbufferedProvider(3)
                    byteArr.toShort(Endian.LITTLE).toInt() to byteArr.last()
                } else {
                    val byteArr = unbufferedProvider(5)
                    byteArr.toInt(Endian.LITTLE) to byteArr.last()
                }

                val layer = appendTo.getOrCreateLayer(ypos)

                val packedPositions = unbufferedProvider(layerByteCount.toInt())
                packedPositions.forEach {
                    Dense16x16BoolMap.unpackPos(it, layer::set)
                }
            }

            return appendTo
        }

        fun deserializeByLayers(yPosAsWord: Boolean, appendTo: DenseIx16x16BoolMap = DenseIx16x16BoolMap(), unbufferedProvider: (Int) -> ByteArray): DenseIx16x16BoolMap {
            val count = unbufferedProvider(4).toInt(Endian.LITTLE)

            @OptIn(ExperimentalUnsignedTypes::class)
            val tempLayer = Dense16x16BoolMap()
            repeat(count) {
                val ypos = if (yPosAsWord) {
                    unbufferedProvider(2).toShort(Endian.LITTLE).toInt()
                } else {
                    unbufferedProvider(4).toInt(Endian.LITTLE)
                }

                val layer = appendTo.getOrCreateLayer(ypos)

                val bytes = unbufferedProvider(32)
                tempLayer.fromPackedBytes(bytes)

                layer.appendFrom(tempLayer)
                tempLayer.clear()
            }

            return appendTo
        }
    }
}