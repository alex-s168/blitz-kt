package blitz.collections

inline fun <reified T> SmartVec(initCap: Int = 0): Vec<T> =
    when (T::class.java) {
        Char::class.java -> CharVec(initCap) as Vec<T>
        Byte::class.java -> ByteVec(initCap) as Vec<T>
        Short::class.java -> ShortVec(initCap) as Vec<T>
        Int::class.java -> IntVec(initCap) as Vec<T>
        Long::class.java -> LongVec(initCap) as Vec<T>
        else -> RefVec(initCap)
    }