package blitz

fun <R> modifyException(new: Throwable, block: () -> R): R =
    try {
        block()
    } catch (e: Throwable) {
        throw new
    }