package blitz.ice

interface Freezable {
    fun freeze()
    fun isFrozen(): Boolean
}

inline fun <R> Freezable.map(block: (Freezable) -> R): R? =
    if (isFrozen()) null
    else block(this)