import blitz.collections.DenseIx16x16BoolMap
import blitz.collections.I2HashMap
import blitz.collections.I2HashMapKey
import blitz.collections.contents
import kotlin.test.Test

class Maps {
    @Test
    fun i2hashmap() {
        val a = I2HashMap<String>(::mutableListOf)
        a[a.index(I2HashMapKey(1, 2390))] = "hi"
        a[a.index(I2HashMapKey(320, 23))] = "bye"
        a[a.index(I2HashMapKey(320, 25))] = "bye2"
        a[a.index(I2HashMapKey(32, 344))] = "bye3"
        println(a.contents())
        println(a.bucketStats())
    }

    @Test
    /** test for: DenseIx16x16BoolMap, SlicedIntKeyMap, Dense16x16BoolMap */
    fun denseI16x16() {
        val a = DenseIx16x16BoolMap()
        a[1, 0, 1] = true
        a[2, 0, 2] = true
        a[3, -1, 4] = true
        a[6, 1, 3] = true
        require(a[1, 0, 1])
        require(!a[1, -1, 1])
        println(a.getSetAsSequence(::Triple).contents)
    }
}