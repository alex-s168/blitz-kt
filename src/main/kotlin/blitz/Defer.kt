package blitz

fun interface DeferScope {
    fun defer(block: () -> Unit)
}

interface ExecutionScope: DeferScope {
    fun onError(block: () -> Unit)

    fun error()

    companion object {
        fun create(
            defer: (block: () -> Unit) -> Unit,
            onError: (block: () -> Unit) -> Unit,
            error: () -> Unit,
        ) = object : ExecutionScope {
            override fun defer(block: () -> Unit) =
                defer(block)

            override fun onError(block: () -> Unit) =
                onError(block)

            override fun error() =
                error()
        }
    }
}

fun <R> resourceScoped(block: ExecutionScope.() -> R): R {
    val defer = mutableListOf<() -> Unit>()
    val onError = mutableListOf<() -> Unit>()
    val scope = ExecutionScope.create(defer::add, onError::add) {
        throw Exception("Manual error triggered")
    }
    val ex: Exception
    try {
        return block(scope)
    } catch (e: Exception) {
        ex = e
        onError.forEach { it.invoke() }
    } finally {
        defer.forEach { it.invoke() }
    }
    throw ex
}