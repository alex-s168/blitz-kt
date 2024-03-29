package blitz.ice

class Cooled<T>(private val of: T): Freezable {
    private var frozen = false

    override fun freeze() {
        frozen = true
    }

    override fun isFrozen(): Boolean {
        return frozen
    }

    fun getOrNull(): T? =
        if (isFrozen()) null else of

    fun <R> use(block: (T) -> R): R? =
        if (isFrozen()) null
        else block(of)
}