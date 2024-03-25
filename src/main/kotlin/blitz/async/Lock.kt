package blitz.async

@JvmInline
value class Lock private constructor(
    private val impl: Any
) {
    constructor(): this(Any())

    fun <R> use(block: () -> R): R =
        synchronized(impl, block)
}