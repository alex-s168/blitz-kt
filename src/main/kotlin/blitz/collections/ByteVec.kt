package blitz.collections

class ByteVec(initCap: Int = 0): Vec<Byte>, ByteBatchSequence {
    override var size = 0
    private var cap = initCap
    private var array = ByteArray(initCap)

    fun copyAsArray(): ByteArray =
        array.copyOfRange(0, size)

    fun copyIntoArray(arr: ByteArray, destOff: Int = 0, startOff: Int = 0) =
        array.copyInto(arr, destOff, startOff, size)

    override fun copy(): ByteVec =
        ByteVec(size).also {
            copyIntoArray(it.array)
        }

    override fun reserve(amount: Int)  {
        if (amount > 0 && cap - size >= amount)
            return
        array = array.copyOf(size + amount)
        cap = size + amount
    }

    override fun popBack(): Byte =
        array[size - 1].also {
            reserve(-1)
            size --
        }

    fun popBack(dest: ByteArray, destOff: Int = 0) {
        copyIntoArray(dest, destOff, size - dest.size)
        reserve(-dest.size)
        size -= dest.size
    }

    override fun get(index: Int): Byte =
        array[index]

    override fun flip() {
        array = array.reversedArray()
    }

    fun pushBack(arr: ByteArray) {
        reserve(arr.size)
        arr.copyInto(array, size)
        size += arr.size
    }

    override fun pushBack(elem: Byte) {
        reserve(8)
        array[size] = elem
        size ++
    }

    override fun iterator(): ByteBatchIterator =
        array.asSequence().asBatch().iterator().asByteBatchIterator()

    override fun toString(): String =
        joinToString(prefix = "[", postfix = "]") { "0x${it.toUByte().toString(16)}" }

    override fun set(index: Int, value: Byte) {
        array[index] = value
    }

    companion object {
        fun from(bytes: ByteArray) =
            ByteVec(bytes.size).also {
                bytes.copyInto(it.array)
                it.size += bytes.size
            }

        fun from(bytes: Sequence<Byte>) =
            ByteVec().also { bv ->
                bytes.forEach(bv::pushBack)
            }
    }
}