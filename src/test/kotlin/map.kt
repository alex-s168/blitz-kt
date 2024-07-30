import blitz.collections.I2HashMap
import blitz.collections.I2HashMapKey
import kotlin.test.Test

class Maps {
    @Test
    fun i2hashmap() {
        val a = I2HashMap<String>(::mutableListOf)
        a[a.index(I2HashMapKey(1, 2390))] = "hi"
        a[a.index(I2HashMapKey(320, 23))] = "bye"
        a[a.index(I2HashMapKey(320, 25))] = "bye2"
        a[a.index(I2HashMapKey(32, 344))] = "bye3"
        println(a.contents)
        println(a.bucketStats)
    }
}