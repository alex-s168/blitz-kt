package blitz.logic

/**
 * Execute a block if the boolean is true.
 */
inline fun Boolean.then(block: () -> Unit): Boolean {
    if (this) {
        block()
    }

    return this
}

/**
 * Execute a block if the boolean is false.
 */
inline fun Boolean.otherwise(block: () -> Unit): Boolean {
    if (!this) {
        block()
    }

    return this
}

/**
 * Execute a block if the boolean is true, otherwise execute another block.
 */
inline fun Boolean.then(block: () -> Unit, otherwise: () -> Unit): Boolean {
    if (this) {
        block()
    } else {
        otherwise()
    }

    return this
}