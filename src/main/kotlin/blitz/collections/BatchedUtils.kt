package blitz.collections

fun ByteBatchSequence.stringify(batch: Int = 64): Sequence<String> {
    val iter = iterator()
    return generateSequence {
        if (iter.hasNext())
            iter.nextBytes(batch).decodeToString()
        else null
    }
}